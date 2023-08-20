# MySQL INIT
import mysql.connector as connector

mysql = {
  "host": "",
  "user": "",
  "password": "",
  "database": ""
}
tableNames = {
    "bans": "minestar_bans",
    "warns": "minestar_warns"
}

db = connector.connect(
  host=(mysql["host"]),
  user=mysql["user"],
  password=mysql["password"],
  database=mysql["database"]
)

# Parsing
import time, datetime
import json
import yaml

curr_time = round(time.time() * 1000)

table = {}

# User Cache
with open("../../../../../../../Users/An1by/Desktop/restart/usercache.json", encoding="utf8") as usercacheFile:
    data = json.loads(usercacheFile.read())
    for i in data:
        table[i["uuid"]] = i["name"]

# Bans
banTable = {}
with open("../../../../../../../Users/An1by/Desktop/restart/banned-players.json", encoding="utf8") as banfile:
    data = json.loads(banfile.read())
    for i in data:
        banTable[i["name"]] = {
            "reason": i["reason"],
            "admin": i["source"]
        }

# Warns
with open("../../../../../../../Users/An1by/Desktop/restart/config_1.yml", "r", encoding="utf8") as yamlfile:
    data = yaml.load(yamlfile, Loader=yaml.FullLoader)

warnList = []

for intruderUUID in data["players"]:
    obj = data["players"][intruderUUID]
    if (obj and obj != {}):
        intruder = table[intruderUUID]

        localOrange = []
        localRed = []

        for warnNumber in obj:
            warn = obj[warnNumber]

            admin = table[warn["reporter"]]
            warnType = warn["type"]
            expireTime = warn["expireTime"] if warnType != "RED_WARN" else None
            victim = "per(" + warnNumber + ")" if warnType == "YELLOW_WARN" else None
            jsonData = {
                "intruder": intruder,
                "admin": admin,
                "expireTime": expireTime,
                "reason": warnNumber,
                "victim": victim,
                "number": warnNumber
            }
            match (warnType):
                case "YELLOW_WARN":
                    warnList.append(jsonData)
                case "ORANGE_WARN":
                    if curr_time < expireTime:
                        localOrange.append(jsonData)
                case "RED_WARN":
                    localRed.append(jsonData)
        
        # if len(localOrange) >= 2:
        #     strR = "(Old#%s, Old#%s)" % (localOrange[0]["number"], localOrange[1]["number"])
        #     localRed.append({
        #         "intruder": intruder,
        #         "admin": "Система",
        #         "expireTime": None,
        #         "reason": "2 временных предупреждения " + strR,
        #         "victim": None
        #     })
        #     localOrange = []
        
        # if len(localRed) >= 3:
        #     if (not (intruder in banTable)):
        #         strR = "(Old#%s, Old#%s, Old#%s)" % (localRed[0]["number"], localRed[1]["number"], localRed[2]["number"])
        #         banTable[intruder] = {
        #             "reason": "3 предупреждения " + strR,
        #             "admin": "Система"
        #         }
        #     localRed = []
        
        warnList.extend(localOrange)
        warnList.extend(localRed)

# MySQL Inserts Bans
sqlBans = "INSERT INTO " + tableNames["bans"] + " (intruder, admin, reason, server) VALUES  (%s, %s, %s, %s);"
valBans = []
for intruder in banTable:
    ban = banTable[intruder]
    server = "main" if "3 предупреждения (" in ban["reason"] else "all"
    valBans.append((intruder, ban["admin"], ban["reason"], server))

db.cursor().executemany(sqlBans, valBans)

# MySQL Inserts Warns
sqlWarns = "INSERT INTO " + tableNames["warns"] + " (intruder, admin, victim, reason, expireTime, server) VALUES  (%s, %s, %s, %s, %s, %s);"
val = []
sqlWarns2 = "INSERT INTO " + tableNames["warns"] + " (intruder, admin, victim, reason, server) VALUES  (%s, %s, %s, %s, %s);"
val2 = []
for warn in warnList:
    if warn["expireTime"]:
        timestamp = datetime.datetime.fromtimestamp(round(warn["expireTime"] / 1000)).strftime('%Y-%m-%d %H:%M:%S')
        val.append((warn["intruder"], warn["admin"], warn["victim"], warn["reason"], timestamp, "main"))
    else:
        val2.append((warn["intruder"], warn["admin"], warn["victim"], warn["reason"], "main"))

db.cursor().executemany(sqlWarns, val)
db.cursor().executemany(sqlWarns2, val2)
db.commit()