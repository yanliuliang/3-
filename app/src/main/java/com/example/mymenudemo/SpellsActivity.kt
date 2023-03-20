package com.example.mymenudemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.example.mymenudemo.databinding.ActivitySpellsBinding
import com.example.mymenudemo.databinding.LayoutContentTabItemBinding
import com.google.android.flexbox.FlexboxLayoutManager

class SpellsActivity :AppCompatActivity() {
    private val viewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
            SpellViewModel::class.java
        )
    }
    private lateinit var   binding:ActivitySpellsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_spells)
        binding.lifecycleOwner = this
        viewModel.getData()
        initTab()
        initObserver()
    }
    private fun initTab() {
        binding.layoutSpells.vm = viewModel
        binding.layoutRandom.vm = viewModel
        binding.layoutBottomEdit.vm = viewModel

        binding.rvContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = binding.rvContent.layoutManager as LinearLayoutManager
                    val topItemId = layoutManager.findFirstVisibleItemPosition()
                    binding.rvTop.scrollToPosition(topItemId)
                    viewModel.changeTopAndLeftSelect(topItemId) {
                        binding.rvLeft.scrollToPosition(it)
                    }
                }
            }
        })
        binding.rvContent.linear().setup {
            addType<ThemeListBean>(R.layout.layout_content_tab_item)
            onCreate {
                getBinding<LayoutContentTabItemBinding>().rvTable.setup {
                    addType<ListDataBean>(R.layout.item_label)
                    onClick(R.id.tv_table) {
                        viewModel.clickContentData(getModel(), modelPosition, true)
                    }
                }
            }
            onBind {
                val data = getModel<ThemeListBean>(modelPosition)
                getBinding<LayoutContentTabItemBinding>().rvTable.layoutManager =
                    FlexboxLayoutManager(this@SpellsActivity)
                getBinding<LayoutContentTabItemBinding>().rvTable.models = data.list
            }
        }
        binding.rvTop.linear(LinearLayoutManager.HORIZONTAL).setup {
            addType<ThemeListBean>(R.layout.layout_top_tab_item)
            onClick(R.id.layout) {
                binding.rvContent.scrollToPosition(modelPosition)
                viewModel.clickTopData(modelPosition)
            }
            onBind {
                if (modelPosition == 0) {
                    this.findView<ConstraintLayout>(R.id.layout)
                        .updateLayoutParams<RecyclerView.LayoutParams> {
                            marginStart = DensityUtil.dp2px(this@SpellsActivity, 9f)
                        }
                }
            }
        }
        binding.rvLeft.linear().setup {
            addType<TableListBean>(R.layout.layout_left_tab_item)
            onClick(R.id.layout) {
                val data = getModel<TableListBean>()
                binding.rvContent.scrollToPosition(data.childId)
                binding.rvTop.scrollToPosition(data.childId)
                viewModel.clickTabLeftData(modelPosition)
            }
        }

        binding.layoutBottomEdit.rvTag.layoutManager = FlexboxLayoutManager(this)
        binding.layoutBottomEdit.rvTag.setup {
            addType<TagSelectBean>(R.layout.layout_tag_edit)
            onClick(R.id.tv_tag){
                val data = getModel<TagSelectBean>()
                viewModel.clickTag(data,modelPosition)
            }
            onClick(R.id.iv_tgClose){
                val data = getModel<TagSelectBean>()
                viewModel.removeTag(data)
            }
        }
    }

    private fun initObserver() {
        viewModel.tableBean.observe(this) {
            binding.rvLeft.models = it
        }
        viewModel.themeBean.observe(this) {
            binding.rvTop.models = it
            binding.rvContent.models = it
        }
        viewModel.showTagList.observe(this) {
            binding.layoutBottomEdit.rvTag.models = it
        }
    }
}