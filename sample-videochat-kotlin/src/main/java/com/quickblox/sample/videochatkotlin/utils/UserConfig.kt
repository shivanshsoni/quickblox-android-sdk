package com.quickblox.sample.videochatkotlin.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quickblox.users.model.QBUser
import java.io.BufferedReader


/**
 * Created by Roman on 08.04.2018.
 */

fun getAllUsersFromFile(filename: String, context: Context): ArrayList<QBUser> {
    val jsonInputStream = context.assets.open(filename)
    val jsonUsers = jsonInputStream.bufferedReader().use(BufferedReader::readText)
    jsonInputStream.close()

    val type = object : TypeToken<Map<String, String>>() {}.type
    val userMap: Map<String, String> = Gson().fromJson(jsonUsers, type)

    val qbUsers = ArrayList<QBUser>()
    userMap.forEach { login, password -> qbUsers.add(QBUser(login, password)) }
    return qbUsers
}

fun getIdsOpponents(selectedUsers: Collection<QBUser>): ArrayList<Int> {
    val opponentsIds = ArrayList<Int>()
    if (!selectedUsers.isEmpty()) {
        for (qbUser in selectedUsers) {
            opponentsIds.add(qbUser.id)
        }
    }
    return opponentsIds
}