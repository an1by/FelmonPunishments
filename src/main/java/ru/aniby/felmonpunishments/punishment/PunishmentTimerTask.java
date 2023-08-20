package ru.aniby.felmonpunishments.punishment;

import ru.aniby.felmonpunishments.punishment.ban.BanManager;
import ru.aniby.felmonpunishments.punishment.mute.MuteManager;
import ru.aniby.felmonpunishments.punishment.warn.WarnManager;

import java.util.TimerTask;

public class PunishmentTimerTask extends TimerTask {
    @Override
    public void run() {
        BanManager.check();
        MuteManager.check();
        WarnManager.check();
    }
}
