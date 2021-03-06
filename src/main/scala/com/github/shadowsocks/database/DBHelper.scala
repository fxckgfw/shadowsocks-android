/*
 * Shadowsocks - A shadowsocks client for Android
 * Copyright (C) 2013 <max.c.lv@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *                            ___====-_  _-====___
 *                      _--^^^#####//      \\#####^^^--_
 *                   _-^##########// (    ) \\##########^-_
 *                  -############//  |\^^/|  \\############-
 *                _/############//   (@::@)   \\############\_
 *               /#############((     \\//     ))#############\
 *              -###############\\    (oo)    //###############-
 *             -#################\\  / VV \  //#################-
 *            -###################\\/      \//###################-
 *           _#/|##########/\######(   /\   )######/\##########|\#_
 *           |/ |#/\#/\#/\/  \#/\##\  |  |  /##/\#/  \/\#/\#/\#| \|
 *           `  |/  V  V  `   V  \#\| |  | |/#/  V   '  V  V  \|  '
 *              `   `  `      `   / | |  | | \   '      '  '   '
 *                               (  | |  | |  )
 *                              __\ | |  | | /__
 *                             (vvv(VVV)(VVV)vvv)
 *
 *                              HERE BE DRAGONS
 *
 */

package com.github.shadowsocks.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils

object DBHelper {
  val PROFILE = "profile.db"
}

class DBHelper(val context: Context)
  extends OrmLiteSqliteOpenHelper(context, DBHelper.PROFILE, null, 13) {

  lazy val profileDao: Dao[Profile, Int] = getDao(classOf[Profile])

  def onCreate(database: SQLiteDatabase, connectionSource: ConnectionSource) {
    TableUtils.createTable(connectionSource, classOf[Profile])
  }

  def onUpgrade(database: SQLiteDatabase, connectionSource: ConnectionSource, oldVersion: Int,
    newVersion: Int) {
    if (oldVersion != newVersion) {
      if (oldVersion < 7) {
        profileDao.executeRawNoArgs("DROP TABLE IF EXISTS 'profile';")
        onCreate(database, connectionSource)
        return
      }
      if (oldVersion < 8) {
        profileDao.executeRawNoArgs("ALTER TABLE `profile` ADD COLUMN udpdns SMALLINT;")
        profileDao.executeRawNoArgs("ALTER TABLE `profile` ADD COLUMN route VARCHAR;")
      }
      if (oldVersion < 9) {
        profileDao.executeRawNoArgs("ALTER TABLE `profile` ADD COLUMN route VARCHAR;")
      }
      if (oldVersion < 10) {
        profileDao.executeRawNoArgs("ALTER TABLE `profile` ADD COLUMN auth SMALLINT;")
      }
      if (oldVersion < 11) {
        profileDao.executeRawNoArgs("ALTER TABLE `profile` ADD COLUMN ipv6 SMALLINT;")
      }
      if (oldVersion < 12) {
        profileDao.executeRawNoArgs("BEGIN TRANSACTION;")
        profileDao.executeRawNoArgs("ALTER TABLE `profile` RENAME TO `tmp`;")
        TableUtils.createTable(connectionSource, classOf[Profile])
        profileDao.executeRawNoArgs(
          "INSERT INTO `profile`(id, name, host, localPort, remotePort, password, method, route, proxyApps, bypass," +
            " udpdns, auth, ipv6, individual) " +
          "SELECT id, name, host, localPort, remotePort, password, method, route, 1 - global, bypass, udpdns, auth," +
          " ipv6, individual FROM `tmp`;")
        profileDao.executeRawNoArgs("DROP TABLE `tmp`;")
        profileDao.executeRawNoArgs("COMMIT;")
        return
      }
      if (oldVersion < 13) {
        profileDao.executeRawNoArgs("ALTER TABLE `profile` ADD COLUMN tx LONG;")
        profileDao.executeRawNoArgs("ALTER TABLE `profile` ADD COLUMN rx LONG;")
        profileDao.executeRawNoArgs("ALTER TABLE `profile` ADD COLUMN date VARCHAR;")
      }
    }
  }
}
