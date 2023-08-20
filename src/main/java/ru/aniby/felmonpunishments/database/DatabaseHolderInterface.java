package ru.aniby.felmonpunishments.database;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DatabaseHolderInterface {
    int getId();
    void setId(int id);

    default void load() {}

    default void update() {}

    default int save() {
        if (this.getId() > 0)
            return this.getId();
        return searchInDatabase();
    }
    default int searchInDatabase() {
        return this.getId();
    }

    default void remove() {}

    default @NotNull JsonObject toJSON() {
        return new JsonObject();
    }

    String database = null;

    static @Nullable <T extends DatabaseHolderInterface> T parseJSON(JsonObject jsonObject) {
        return null;
    }
}
