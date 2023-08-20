# MySQL INIT
import mysql.connector as connector

mysql = {
    "host": "",
    "user": "",
    "password": "",
    "database": "minestar"
}
tableName = "minestar_players"

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

usercache = {}

# User Cache
with open("./data/usercache.json", encoding="utf8") as usercacheFile:
    data = json.loads(usercacheFile.read())
    for i in data:
        usercache[i["uuid"]] = i["name"]

to_input = {}

# linked accounts
linkTable = {}
with open("./data/linkedaccounts.json", encoding="utf8") as banfile:
    data = json.loads(banfile.read())
    for discordId in data:
        uuid = data[discordId]
        if uuid in usercache:
            username = usercache[uuid]
            to_input[username] = discordId



sqlPlayers = "INSERT INTO " + tableName + " (username, discord) VALUES  (%s, %s);"
valPlayers = []
for username in to_input:
    discordId = to_input[username]
    valPlayers.append((username, discordId))

db.cursor().executemany(sqlPlayers, valPlayers)
db.commit()