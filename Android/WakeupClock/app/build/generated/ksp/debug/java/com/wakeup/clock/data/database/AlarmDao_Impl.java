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
import com.wakeup.clock.data.model.AlarmModel;
import com.wakeup.clock.data.model.Difficulty;
import com.wakeup.clock.data.model.MissionType;
import com.wakeup.clock.data.model.RepeatMode;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AlarmDao_Impl implements AlarmDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AlarmModel> __insertionAdapterOfAlarmModel;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<AlarmModel> __deletionAdapterOfAlarmModel;

  private final EntityDeletionOrUpdateAdapter<AlarmModel> __updateAdapterOfAlarmModel;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAlarmById;

  private final SharedSQLiteStatement __preparedStmtOfSetAlarmEnabled;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllAlarms;

  public AlarmDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAlarmModel = new EntityInsertionAdapter<AlarmModel>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `alarms` (`id`,`time`,`enabled`,`label`,`missionType`,`difficulty`,`repeatMode`,`customDays`,`skipHolidays`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlarmModel entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTime());
        final int _tmp = entity.getEnabled() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindString(4, entity.getLabel());
        final String _tmp_1 = __converters.fromMissionType(entity.getMissionType());
        statement.bindString(5, _tmp_1);
        final int _tmp_2 = __converters.fromDifficulty(entity.getDifficulty());
        statement.bindLong(6, _tmp_2);
        final String _tmp_3 = __converters.fromRepeatMode(entity.getRepeatMode());
        statement.bindString(7, _tmp_3);
        final String _tmp_4 = __converters.fromIntList(entity.getCustomDays());
        statement.bindString(8, _tmp_4);
        final int _tmp_5 = entity.getSkipHolidays() ? 1 : 0;
        statement.bindLong(9, _tmp_5);
        statement.bindLong(10, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfAlarmModel = new EntityDeletionOrUpdateAdapter<AlarmModel>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `alarms` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlarmModel entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfAlarmModel = new EntityDeletionOrUpdateAdapter<AlarmModel>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `alarms` SET `id` = ?,`time` = ?,`enabled` = ?,`label` = ?,`missionType` = ?,`difficulty` = ?,`repeatMode` = ?,`customDays` = ?,`skipHolidays` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlarmModel entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTime());
        final int _tmp = entity.getEnabled() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindString(4, entity.getLabel());
        final String _tmp_1 = __converters.fromMissionType(entity.getMissionType());
        statement.bindString(5, _tmp_1);
        final int _tmp_2 = __converters.fromDifficulty(entity.getDifficulty());
        statement.bindLong(6, _tmp_2);
        final String _tmp_3 = __converters.fromRepeatMode(entity.getRepeatMode());
        statement.bindString(7, _tmp_3);
        final String _tmp_4 = __converters.fromIntList(entity.getCustomDays());
        statement.bindString(8, _tmp_4);
        final int _tmp_5 = entity.getSkipHolidays() ? 1 : 0;
        statement.bindLong(9, _tmp_5);
        statement.bindLong(10, entity.getCreatedAt());
        statement.bindString(11, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAlarmById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM alarms WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetAlarmEnabled = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE alarms SET enabled = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllAlarms = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM alarms";
        return _query;
      }
    };
  }

  @Override
  public Object insertAlarm(final AlarmModel alarm, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAlarmModel.insert(alarm);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAlarm(final AlarmModel alarm, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAlarmModel.handle(alarm);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateAlarm(final AlarmModel alarm, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAlarmModel.handle(alarm);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAlarmById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAlarmById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteAlarmById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setAlarmEnabled(final String id, final boolean enabled,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetAlarmEnabled.acquire();
        int _argIndex = 1;
        final int _tmp = enabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfSetAlarmEnabled.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllAlarms(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllAlarms.acquire();
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
          __preparedStmtOfDeleteAllAlarms.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AlarmModel>> getAllAlarms() {
    final String _sql = "SELECT * FROM alarms ORDER BY time ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"alarms"}, new Callable<List<AlarmModel>>() {
      @Override
      @NonNull
      public List<AlarmModel> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfMissionType = CursorUtil.getColumnIndexOrThrow(_cursor, "missionType");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfRepeatMode = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatMode");
          final int _cursorIndexOfCustomDays = CursorUtil.getColumnIndexOrThrow(_cursor, "customDays");
          final int _cursorIndexOfSkipHolidays = CursorUtil.getColumnIndexOrThrow(_cursor, "skipHolidays");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlarmModel> _result = new ArrayList<AlarmModel>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlarmModel _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final boolean _tmpEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp != 0;
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final MissionType _tmpMissionType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMissionType);
            _tmpMissionType = __converters.toMissionType(_tmp_1);
            final Difficulty _tmpDifficulty;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfDifficulty);
            _tmpDifficulty = __converters.toDifficulty(_tmp_2);
            final RepeatMode _tmpRepeatMode;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfRepeatMode);
            _tmpRepeatMode = __converters.toRepeatMode(_tmp_3);
            final List<Integer> _tmpCustomDays;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfCustomDays);
            _tmpCustomDays = __converters.toIntList(_tmp_4);
            final boolean _tmpSkipHolidays;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfSkipHolidays);
            _tmpSkipHolidays = _tmp_5 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlarmModel(_tmpId,_tmpTime,_tmpEnabled,_tmpLabel,_tmpMissionType,_tmpDifficulty,_tmpRepeatMode,_tmpCustomDays,_tmpSkipHolidays,_tmpCreatedAt);
            _result.add(_item);
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
  public Flow<List<AlarmModel>> getEnabledAlarms() {
    final String _sql = "SELECT * FROM alarms WHERE enabled = 1 ORDER BY time ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"alarms"}, new Callable<List<AlarmModel>>() {
      @Override
      @NonNull
      public List<AlarmModel> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfMissionType = CursorUtil.getColumnIndexOrThrow(_cursor, "missionType");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfRepeatMode = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatMode");
          final int _cursorIndexOfCustomDays = CursorUtil.getColumnIndexOrThrow(_cursor, "customDays");
          final int _cursorIndexOfSkipHolidays = CursorUtil.getColumnIndexOrThrow(_cursor, "skipHolidays");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlarmModel> _result = new ArrayList<AlarmModel>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlarmModel _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final boolean _tmpEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp != 0;
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final MissionType _tmpMissionType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMissionType);
            _tmpMissionType = __converters.toMissionType(_tmp_1);
            final Difficulty _tmpDifficulty;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfDifficulty);
            _tmpDifficulty = __converters.toDifficulty(_tmp_2);
            final RepeatMode _tmpRepeatMode;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfRepeatMode);
            _tmpRepeatMode = __converters.toRepeatMode(_tmp_3);
            final List<Integer> _tmpCustomDays;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfCustomDays);
            _tmpCustomDays = __converters.toIntList(_tmp_4);
            final boolean _tmpSkipHolidays;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfSkipHolidays);
            _tmpSkipHolidays = _tmp_5 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlarmModel(_tmpId,_tmpTime,_tmpEnabled,_tmpLabel,_tmpMissionType,_tmpDifficulty,_tmpRepeatMode,_tmpCustomDays,_tmpSkipHolidays,_tmpCreatedAt);
            _result.add(_item);
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
  public Object getEnabledAlarmsOnce(final Continuation<? super List<AlarmModel>> $completion) {
    final String _sql = "SELECT * FROM alarms WHERE enabled = 1 ORDER BY time ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlarmModel>>() {
      @Override
      @NonNull
      public List<AlarmModel> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfMissionType = CursorUtil.getColumnIndexOrThrow(_cursor, "missionType");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfRepeatMode = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatMode");
          final int _cursorIndexOfCustomDays = CursorUtil.getColumnIndexOrThrow(_cursor, "customDays");
          final int _cursorIndexOfSkipHolidays = CursorUtil.getColumnIndexOrThrow(_cursor, "skipHolidays");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlarmModel> _result = new ArrayList<AlarmModel>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlarmModel _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final boolean _tmpEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp != 0;
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final MissionType _tmpMissionType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMissionType);
            _tmpMissionType = __converters.toMissionType(_tmp_1);
            final Difficulty _tmpDifficulty;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfDifficulty);
            _tmpDifficulty = __converters.toDifficulty(_tmp_2);
            final RepeatMode _tmpRepeatMode;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfRepeatMode);
            _tmpRepeatMode = __converters.toRepeatMode(_tmp_3);
            final List<Integer> _tmpCustomDays;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfCustomDays);
            _tmpCustomDays = __converters.toIntList(_tmp_4);
            final boolean _tmpSkipHolidays;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfSkipHolidays);
            _tmpSkipHolidays = _tmp_5 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlarmModel(_tmpId,_tmpTime,_tmpEnabled,_tmpLabel,_tmpMissionType,_tmpDifficulty,_tmpRepeatMode,_tmpCustomDays,_tmpSkipHolidays,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAlarmById(final String id, final Continuation<? super AlarmModel> $completion) {
    final String _sql = "SELECT * FROM alarms WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AlarmModel>() {
      @Override
      @Nullable
      public AlarmModel call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfMissionType = CursorUtil.getColumnIndexOrThrow(_cursor, "missionType");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfRepeatMode = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatMode");
          final int _cursorIndexOfCustomDays = CursorUtil.getColumnIndexOrThrow(_cursor, "customDays");
          final int _cursorIndexOfSkipHolidays = CursorUtil.getColumnIndexOrThrow(_cursor, "skipHolidays");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final AlarmModel _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final boolean _tmpEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp != 0;
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final MissionType _tmpMissionType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMissionType);
            _tmpMissionType = __converters.toMissionType(_tmp_1);
            final Difficulty _tmpDifficulty;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfDifficulty);
            _tmpDifficulty = __converters.toDifficulty(_tmp_2);
            final RepeatMode _tmpRepeatMode;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfRepeatMode);
            _tmpRepeatMode = __converters.toRepeatMode(_tmp_3);
            final List<Integer> _tmpCustomDays;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfCustomDays);
            _tmpCustomDays = __converters.toIntList(_tmp_4);
            final boolean _tmpSkipHolidays;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfSkipHolidays);
            _tmpSkipHolidays = _tmp_5 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new AlarmModel(_tmpId,_tmpTime,_tmpEnabled,_tmpLabel,_tmpMissionType,_tmpDifficulty,_tmpRepeatMode,_tmpCustomDays,_tmpSkipHolidays,_tmpCreatedAt);
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
