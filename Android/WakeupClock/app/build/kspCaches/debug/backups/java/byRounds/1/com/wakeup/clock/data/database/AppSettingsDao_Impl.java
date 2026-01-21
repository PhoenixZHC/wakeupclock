package com.wakeup.clock.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.wakeup.clock.data.model.AppSettings;
import com.wakeup.clock.data.model.ThemeMode;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppSettingsDao_Impl implements AppSettingsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AppSettings> __insertionAdapterOfAppSettings;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<AppSettings> __updateAdapterOfAppSettings;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSettings;

  public AppSettingsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAppSettings = new EntityInsertionAdapter<AppSettings>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `app_settings` (`id`,`themeMode`,`language`,`enableAntiSnooze`,`antiSnoozeInterval`,`antiSnoozeCount`,`hasAcceptedSafetyNotice`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppSettings entity) {
        statement.bindLong(1, entity.getId());
        final String _tmp = __converters.fromThemeMode(entity.getThemeMode());
        statement.bindString(2, _tmp);
        statement.bindString(3, entity.getLanguage());
        final int _tmp_1 = entity.getEnableAntiSnooze() ? 1 : 0;
        statement.bindLong(4, _tmp_1);
        statement.bindLong(5, entity.getAntiSnoozeInterval());
        statement.bindLong(6, entity.getAntiSnoozeCount());
        final int _tmp_2 = entity.getHasAcceptedSafetyNotice() ? 1 : 0;
        statement.bindLong(7, _tmp_2);
      }
    };
    this.__updateAdapterOfAppSettings = new EntityDeletionOrUpdateAdapter<AppSettings>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `app_settings` SET `id` = ?,`themeMode` = ?,`language` = ?,`enableAntiSnooze` = ?,`antiSnoozeInterval` = ?,`antiSnoozeCount` = ?,`hasAcceptedSafetyNotice` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppSettings entity) {
        statement.bindLong(1, entity.getId());
        final String _tmp = __converters.fromThemeMode(entity.getThemeMode());
        statement.bindString(2, _tmp);
        statement.bindString(3, entity.getLanguage());
        final int _tmp_1 = entity.getEnableAntiSnooze() ? 1 : 0;
        statement.bindLong(4, _tmp_1);
        statement.bindLong(5, entity.getAntiSnoozeInterval());
        statement.bindLong(6, entity.getAntiSnoozeCount());
        final int _tmp_2 = entity.getHasAcceptedSafetyNotice() ? 1 : 0;
        statement.bindLong(7, _tmp_2);
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteSettings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM app_settings";
        return _query;
      }
    };
  }

  @Override
  public Object insertSettings(final AppSettings settings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAppSettings.insert(settings);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSettings(final AppSettings settings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAppSettings.handle(settings);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSettings(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSettings.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSettings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<AppSettings> getSettings() {
    final String _sql = "SELECT * FROM app_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"app_settings"}, new Callable<AppSettings>() {
      @Override
      @Nullable
      public AppSettings call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfThemeMode = CursorUtil.getColumnIndexOrThrow(_cursor, "themeMode");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfEnableAntiSnooze = CursorUtil.getColumnIndexOrThrow(_cursor, "enableAntiSnooze");
          final int _cursorIndexOfAntiSnoozeInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "antiSnoozeInterval");
          final int _cursorIndexOfAntiSnoozeCount = CursorUtil.getColumnIndexOrThrow(_cursor, "antiSnoozeCount");
          final int _cursorIndexOfHasAcceptedSafetyNotice = CursorUtil.getColumnIndexOrThrow(_cursor, "hasAcceptedSafetyNotice");
          final AppSettings _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final ThemeMode _tmpThemeMode;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfThemeMode);
            _tmpThemeMode = __converters.toThemeMode(_tmp);
            final String _tmpLanguage;
            _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            final boolean _tmpEnableAntiSnooze;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEnableAntiSnooze);
            _tmpEnableAntiSnooze = _tmp_1 != 0;
            final int _tmpAntiSnoozeInterval;
            _tmpAntiSnoozeInterval = _cursor.getInt(_cursorIndexOfAntiSnoozeInterval);
            final int _tmpAntiSnoozeCount;
            _tmpAntiSnoozeCount = _cursor.getInt(_cursorIndexOfAntiSnoozeCount);
            final boolean _tmpHasAcceptedSafetyNotice;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfHasAcceptedSafetyNotice);
            _tmpHasAcceptedSafetyNotice = _tmp_2 != 0;
            _result = new AppSettings(_tmpId,_tmpThemeMode,_tmpLanguage,_tmpEnableAntiSnooze,_tmpAntiSnoozeInterval,_tmpAntiSnoozeCount,_tmpHasAcceptedSafetyNotice);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSettingsOnce(final Continuation<? super AppSettings> $completion) {
    final String _sql = "SELECT * FROM app_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AppSettings>() {
      @Override
      @Nullable
      public AppSettings call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfThemeMode = CursorUtil.getColumnIndexOrThrow(_cursor, "themeMode");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfEnableAntiSnooze = CursorUtil.getColumnIndexOrThrow(_cursor, "enableAntiSnooze");
          final int _cursorIndexOfAntiSnoozeInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "antiSnoozeInterval");
          final int _cursorIndexOfAntiSnoozeCount = CursorUtil.getColumnIndexOrThrow(_cursor, "antiSnoozeCount");
          final int _cursorIndexOfHasAcceptedSafetyNotice = CursorUtil.getColumnIndexOrThrow(_cursor, "hasAcceptedSafetyNotice");
          final AppSettings _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final ThemeMode _tmpThemeMode;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfThemeMode);
            _tmpThemeMode = __converters.toThemeMode(_tmp);
            final String _tmpLanguage;
            _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            final boolean _tmpEnableAntiSnooze;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEnableAntiSnooze);
            _tmpEnableAntiSnooze = _tmp_1 != 0;
            final int _tmpAntiSnoozeInterval;
            _tmpAntiSnoozeInterval = _cursor.getInt(_cursorIndexOfAntiSnoozeInterval);
            final int _tmpAntiSnoozeCount;
            _tmpAntiSnoozeCount = _cursor.getInt(_cursorIndexOfAntiSnoozeCount);
            final boolean _tmpHasAcceptedSafetyNotice;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfHasAcceptedSafetyNotice);
            _tmpHasAcceptedSafetyNotice = _tmp_2 != 0;
            _result = new AppSettings(_tmpId,_tmpThemeMode,_tmpLanguage,_tmpEnableAntiSnooze,_tmpAntiSnoozeInterval,_tmpAntiSnoozeCount,_tmpHasAcceptedSafetyNotice);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
