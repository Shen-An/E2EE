const fs = require('fs');
const sqlite3 = require('sqlite3').verbose();
const os = require('os');
const NODE_ENV = process.env.NODE_ENV;
import { add_tables, alter_tables, add_indexex } from './Tables.js'
const userDir = 'D:'
// console.log(userDir)
const dbFolder = userDir + (NODE_ENV === 'development' ? '/.easyChatDev/' : '/.easyChat/');
// console.log(dbFolder)
if (!fs.existsSync(dbFolder)) {
    fs.mkdirSync(dbFolder);
}
const globalColumnsMap = {};
const db = new sqlite3.Database(dbFolder + 'local.db');
const createTable = () => {
    return new Promise(async (resolve, reject) => {
        for (const item of add_tables) {
            await db.run(item)
        }
        for (const item of alter_tables) {
            await db.run(item)
        }
        for (const item of add_indexex) {
            const fieldList = await queryAll(`PRAGMA table_info(${item.tableName})`, [])
            const field = fieldList.some(row => row.name === item.fields)
            if (!field) {
                await db.run(item)
            }
        }
        resolve()
    });
}


const queryOne = (sql, params) => {
    return new Promise((resolve, reject) => {
        const stmt = db.prepare(sql);
        stmt.get(params, (err, row) => {
            if (err) {
                resolve({})
            }
            resolve(convertDbObj2BizObj(row))
        });
        stmt.finalize()
    });
}

const run = (sql, params) => {
    return new Promise((resolve, reject) => {
        const stmt = db.prepare(sql);
        stmt.get(params, (err, rows) => {
            if (err) {
                resolve("操作数据库失败")
            }
           
            resolve(this.changes)
        });
        stmt.finalize()
    });
}

const insert = (sqlPrefix, tableName, data) => {
    // console.log(data)
    const columnsMap = globalColumnsMap[tableName]
    // console.log(columnsMap)
    const dbColumns = []
    const params = []
    for (let item in data) {
        if (data[item] != undefined && columnsMap[item] != undefined) {
            dbColumns.push(columnsMap[item])
            params.push(data[item])
        }
    }
    const preper = "?".repeat(dbColumns.length).split("").join(",")
    // console.log(preper)
    const sql = `${sqlPrefix} ${tableName}(${dbColumns.join(",")}) values(${preper})`
    // console.log(sql)
    return run(sql, params)
}

const insertOrReplace = (tableName, data) => {
    return insert("insert or replace into", tableName, data)
}
const insertOrIgnore = (tableName, data) => {
    return insert("insert or ignore into", tableName, data)
}

const update = (tableName, data, paramData) => {
    const columnsMap = globalColumnsMap[tableName]
    const dbColumns = []
    const params = []
    const whereColumns = []
    for (let item in data) {
        if (data[item] != undefined && columnsMap[item] != undefined) {
            dbColumns.push(`${columnsMap[item]}=?`)
            params.push(data[item])
        }
    }

    for (let item in data) {
        if (paramData[item]) {
            params.push(paramData[item])
            whereColumns.push(`${columnsMap[item]}=?`)
        }
    }

    const sql = `update ${tableName} set ${dbColumns.join(",")} ${whereColumns.length > 0 ? "where " : ""} ${whereColumns.join(" and ")}`
    return run(sql, params)
}

const queryAll = (sql, params) => {
    return new Promise((resolve, reject) => {
        const stmt = db.prepare(sql);
        stmt.all(params, (err, rows) => {
            if (err) {
                resolve([])
            }
            rows.forEach((item, index) => {
                rows[index] = convertDbObj2BizObj(item)
            })
            resolve(rows)
        });
        stmt.finalize()
    });
}



const queryCount = (sql, params) => {
    return new Promise((resolve, reject) => {
        const stmt = db.prepare(sql);
        stmt.get(params, (err, row) => {
            if (err) {
                resolve(0)
            }
            resolve(Array.from(Object.values(row))[0])
        });
        stmt.finalize()
    });
}

const initTableColumnsMap = async () => {

    let sql = `select name from sqlite_master where type='table' AND name!='sqlite_sequence'`
    let tables = await queryAll(sql, [])
    for (let i = 0; i < tables.length; i++) {
        sql = `PRAGMA table_info(${tables[i].name})`
        let columns = await queryAll(sql, [])
        const columnMapItem = {}
        for (let j = 0; j < columns.length; j++) {
            columnMapItem[toCamelCase(columns[j].name)] = columns[j].name
        }
        globalColumnsMap[tables[i].name] = columnMapItem
    }

    // console.log(globalColumnsMap)
}
const convertDbObj2BizObj = (data) => {
    if (!data) {
        return null;
    }
    const bizData = {};
    for (let item in data) {
        bizData[toCamelCase(item)] = data[item]
    }
    return bizData
}
const toCamelCase = (str) => {
    return str.replace(/_([a-z])/g, function (match, p1) {
        return String.fromCharCode(p1.charCodeAt(0) - 32)
    });
}

const init = () => {
    db.serialize(async () => {
        await createTable()
        await initTableColumnsMap()
    })
}
init()
export {
    run,
    queryOne,
    queryAll,
    queryCount,
    insert,
    insertOrReplace,
    insertOrIgnore,
    update

}