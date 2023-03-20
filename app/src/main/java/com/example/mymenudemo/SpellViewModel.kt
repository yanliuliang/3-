package com.example.mymenudemo

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SpellViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "SpellViewModel"

    /**
     * 一级的数据
     */
    val tableBean = MutableLiveData<MutableList<TableListBean>>()

    /**
     * 二级目录的数据
     */
    val themeBean = MutableLiveData<MutableList<ThemeListBean>>()

    /**
     * 一级菜单第一个子类在全部主体的id
     */
    private var childId = 0

    /**
     * 全部选中的tag
     */
    private var tagList = mutableListOf<ListDataBean>()

    /**
     * tag的数量
     */
    var tagSize = MutableLiveData<Int?>()

    private val random = Random()

    var stringBuffer: String = ""

    /**
     * tag显示框的编辑数据
     */
    val showTagList = MutableLiveData<MutableList<TagSelectBean>?>()
    private val tagUnSelectCount = 1000
    private var tagCloseItemId = 0

    //编辑弹窗是否展示
    var isShow = MutableLiveData(false)


    /**
     * 获取全部数据
     */
    fun getData() {
        CoroutineScope(Dispatchers.IO).launch {
            val bean = LocalDataRepository.getInstance().loadTableListDate()
            val list = mutableListOf<ThemeListBean>()
            for ((position, themeData) in bean.withIndex()) {
                if (position > 0) {
                    //第一个数据不赋值
                    bean[position].childId = childId
                }
                childId += (themeData.list.size)
                list.addAll(themeData.list)
                list[0].isSelect = true
            }
            changeToMain {
                bean[0].select = true
                tableBean.postValue(bean)
                themeBean.postValue(list)
            }

        }
    }

    /**
     * 外部点击左侧标签
     */
    fun clickTabLeftData(position: Int?) {
        changeTabLeftData(position)
    }

    /**
     * 点击左侧标签
     */
    private fun changeTabLeftData(position: Int?) {
        for ((count, bean) in tableBean.value!!.withIndex()) {
            bean.select = position == count
        }
        changeTopData(position?.let { tableBean.value?.get(it)?.childId })
        tableBean.postValue(tableBean.value)
    }

    /**
     * 外部点击头部数据
     */
    fun clickTopData(position: Int?) {
        changeTopData(position)
    }

    /**
     * 点击头部数据
     */
    private fun changeTopData(position: Int?) {
        themeBean.value?.let {
            for ((count, bean) in it.withIndex()) {
                bean.isSelect = position == count
            }
            themeBean.postValue(themeBean.value)
        }
    }

    /**
     * 外部内容标签选中数据处理
     */
    fun clickContentData(
        listDataBean: ListDataBean,
        position: Int = -1,
        isClick: Boolean = false
    ) {
        changeContentData(listDataBean, position, isClick)
    }

    /**
     * 内容标签选中数据处理
     * @isClick: 是否为手动点击  false :随机选中 true 手动点击
     * @id：无用数据 测试数据处理时间顺序，，
     */
    private fun changeContentData(
        listDataBean: ListDataBean,
        position: Int = -1,
        isClick: Boolean = false,
        id: Int = 0
    ) {
        tagCloseItemId = 0
        Log.d(TAG, "changeContentData:开始 $id -----<${System.currentTimeMillis()}>")
        val itemSelectId = if (position == -1) {
            listDataBean.selectCount
        } else {
            position
        }
        listDataBean.selectCount = itemSelectId
        val themeList = themeBean.value?.get(listDataBean.parentThemeId)
        val select = themeList?.list?.get(itemSelectId)?.isSelect
        themeList?.list?.get(itemSelectId)?.isSelect =
            select != true
        if (themeList?.list?.get(itemSelectId)?.isSelect == true) {
            themeList.parentLabel.let {
                tableBean.value?.get(it)?.let { table ->
                    table.count += 1
                }
            }
            stringBuffer += "${listDataBean.typeName}，"
            tagList.add(listDataBean)
        } else {
            themeList?.parentLabel?.let {
                tableBean.value?.get(it)?.let { table ->
                    table.count -= 1
                }
            }
            stringBuffer = stringBuffer.replace("${listDataBean.typeName}，", "")
            tagList.remove(listDataBean)
        }
        Log.d(TAG, "changeContentData:跳转主线程 $id -----<${System.currentTimeMillis()}>")
        changeTagSelect()

        tagSize.value = tagList.size
        themeBean.postValue(themeBean.value)
        tableBean.postValue(tableBean.value)
        if (isClick) {
            changeTabLeftData(themeList?.parentLabel)
            changeTopData(listDataBean.parentThemeId)
        }
        Log.d(TAG, "changeContentData:结束 $id -----<${System.currentTimeMillis()}>")

    }

    private fun changeTagSelect() {
        val showTagBean = mutableListOf<TagSelectBean>()
        var tagId = 0
        Log.d("initObserver", "changeTagSelect: $stringBuffer")
        if (stringBuffer.isNotEmpty()) {
            val stringList = stringBuffer.subSequence(0, stringBuffer.length - 1).toString()
            var list = stringList.split("")
            list = list.subList(1, list.size - 1)
            for (tag in list) {
                if (tag == "，") {
                    showTagBean.add(TagSelectBean(tagUnSelectCount, false, tag, false))
                    tagId += 1
                } else {
                    showTagBean.add(TagSelectBean(tagId, false, tag, false))
                }
            }
            showTagList.postValue(showTagBean)
        } else {
            showTagList.postValue(null)
        }
    }

    /**
     * 滑动内容界面 ，同步左边和上边的选中
     */
    fun changeTopAndLeftSelect(position: Int, block: (left: Int) -> Unit) {
        val themeData = themeBean.value?.get(position)
        themeData?.apply {
            block.invoke(this.parentLabel)
            changeTabLeftData(this.parentLabel)
        }
    }

    /**
     * 点击随机按钮
     */
    fun getRandomData() {
        var i = 0
        tableBean.value?.let { value ->
            for (bean in value) {
                i++
                if (bean.count != 0) {
                    continue
                }
                val themeCount = bean.list.size
                val themeList = bean.list[random.nextInt(themeCount)]
                val listCount = themeList.list.size
                val randomSelectId = random.nextInt(listCount)
                val randomData = themeList.list[randomSelectId]
                if ((stringBuffer + "${randomData.typeName},").length > 100) {
                    // TODO:
                    return
                } else {
                    changeContentData(randomData, randomSelectId, id = i)
                }
            }
        }
    }

    fun clickLoadMoreTag() {
        isShow.value = isShow.value != true
    }

    fun clickTag(data: TagSelectBean, modelPosition: Int) {
        val selectId = data.tagId
        if (data.select || selectId == tagUnSelectCount) {
            return
        }
        var lastItem = 0
        showTagList.value?.apply {
            if (tagCloseItemId != 0 && tagCloseItemId < this.size) {
                this.removeAt(tagCloseItemId)
            }
            for ((position, bean) in this.withIndex()) {
                if (bean.tagId == selectId) {
                    bean.select = true
                    lastItem = position
                } else {
                    bean.select = false
                }
            }
            tagCloseItemId = lastItem + 1
            this.add(tagCloseItemId, TagSelectBean(selectId, true, "", true))
        }
        showTagList.postValue(showTagList.value)
    }

    /**
     * 删除某个标签
     */
    fun removeTag(data: TagSelectBean) {
        changeContentData(tagList[data.tagId])
    }

    /**
     * @Synchronized 同步执行数据处理，等待
     */
    @Synchronized
    private fun taskSynchronize(block: () -> Unit) {
        block.invoke()
    }

    /**
     * 切回主线程
     */
    private fun changeToMain(block: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            block.invoke()
        }
    }


}