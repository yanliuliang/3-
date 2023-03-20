package com.example.mymenudemo


import com.google.gson.Gson
import com.google.gson.internal.`$Gson$Types`
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class LocalDataRepository private constructor() {

    suspend fun loadTableListDate() = withContext(Dispatchers.IO) {
        await(TableListBean::class.java, "table_list.json")
    }
    private suspend fun <T> await(clazz: Class<T>, fileName: String): MutableList<T> {
        return suspendCoroutine {
            val bufferedReader = MyApplication.instance.assets.open(fileName).bufferedReader()
            val sb = StringBuffer()
            while (true) {
                //当有内容时读取一行数据，否则退出循环
                val line = bufferedReader.readLine() ?: break
                sb.append(line)
            }
            val type =
                `$Gson$Types`.newParameterizedTypeWithOwner(null, ArrayList::class.java, clazz)
            val data = Gson().fromJson<MutableList<T>>(sb.toString(), type)
            bufferedReader.close()
            it.resume(data)
        }
    }


    companion object {

        private var instance: LocalDataRepository? = null

        fun getInstance(): LocalDataRepository {
            if (instance == null) {
                synchronized(LocalDataRepository::class.java) {
                    if (instance == null) {
                        instance = LocalDataRepository()
                    }
                }
            }
            return instance!!
        }

    }

}