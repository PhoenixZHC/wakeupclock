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
import com.wakeup.clock.data.model.WakeUpRecord;
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
public final class WakeUpRecordDao_Impl implements WakeUpRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WakeUpRecord> __insertionAdapterOfWakeUpRecord;

  private final EntityDeletionOrUpdateAdapter<WakeUpRecord> __deletionAdapterOfWakeUpRecord;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllRecords;

  public WakeUpRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWakeUpRecord = new EntityInsertionAdapter<WakeUpRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `wakeup_records` (`id`,`date`,`time`,`alarmLabel`,`alarmId`,`timestamp`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WakeUpRecord entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getDate());
        statement.bindString(3, entity.getTime());
        if (entity.getAlarmLabel() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAlarmLabel());
        }
        if (entity.getAlarmId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAlarmId());
        }
        statement.bindLong(6, entity.getTimestamp());
      }
    };
    this.__deletionAdapterOfWakeUpRecord = new EntityDeletionOrUpdateAdapter<WakeUpRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `wakeup_records` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WakeUpRecord entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllRecords = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM wakeup_records";
        return _query;
      }
    };
  }

  @Override
  public Object insertRecord(final WakeUpRecord record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWakeUpRecord.insert(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRecord(final WakeUpRecord record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfWakeUpRecord.handle(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllRecords(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllRecords.acquire();
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
          __preparedStmtOfDeleteAllRecords.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<WakeUpRecord>> getAllRecords() {
    final String _sql = "SELECT * FROM wakeup_records ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"wakeup_records"}, new Callable<List<WakeUpRecord>>() {
      @Override
      @NonNull
      public List<WakeUpRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfAlarmLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmLabel");
          final int _cursorIndexOfAlarmId = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<WakeUpRecord> _result = new ArrayList<WakeUpRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WakeUpRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpAlarmLabel;
            if (_cursor.isNull(_cursorIndexOfAlarmLabel)) {
              _tmpAlarmLabel = null;
            } else {
              _tmpAlarmLabel = _cursor.getString(_cursorIndexOfAlarmLabel);
            }
            final String _tmpAlarmId;
            if (_cursor.isNull(_cursorIndexOfAlarmId)) {
              _tmpAlarmId = null;
            } else {
              _tmpAlarmId = _cursor.getString(_cursorIndexOfAlarmId);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new WakeUpRecord(_tmpId,_tmpDate,_tmpTime,_tmpAlarmLabel,_tmpAlarmId,_tmpTimestamp);
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
  public Flow<List<WakeUpRecord>> getRecordsByMonth(final String yearMonth) {
    final String _sql = "SELECT * FROM wakeup_records WHERE date LIKE ? || '%' ORDER BY date ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, yearMonth);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"wakeup_records"}, new Callable<List<WakeUpRecord>>() {
      @Override
      @NonNull
      public List<WakeUpRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfAlarmLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmLabel");
          final int _cursorIndexOfAlarmId = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<WakeUpRecord> _result = new ArrayList<WakeUpRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WakeUpRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpAlarmLabel;
            if (_cursor.isNull(_cursorIndexOfAlarmLabel)) {
              _tmpAlarmLabel = null;
            } else {
              _tmpAlarmLabel = _cursor.getString(_cursorIndexOfAlarmLabel);
            }
            final String _tmpAlarmId;
            if (_cursor.isNull(_cursorIndexOfAlarmId)) {
              _tmpAlarmId = null;
            } else {
              _tmpAlarmId = _cursor.getString(_cursorIndexOfAlarmId);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new WakeUpRecord(_tmpId,_tmpDate,_tmpTime,_tmpAlarmLabel,_tmpAlarmId,_tmpTimestamp);
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
  public Object getRecordByDate(final String date,
      final Continuation<? super WakeUpRecord> $completion) {
    final String _sql = "SELECT * FROM wakeup_records WHERE date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WakeUpRecord>() {
      @Override
      @Nullable
      public WakeUpRecord call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfAlarmLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmLabel");
          final int _cursorIndexOfAlarmId = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final WakeUpRecord _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpAlarmLabel;
            if (_cursor.isNull(_cursorIndexOfAlarmLabel)) {
              _tmpAlarmLabel = null;
            } else {
              _tmpAlarmLabel = _cursor.getString(_cursorIndexOfAlarmLabel);
            }
            final String _tmpAlarmId;
            if (_cursor.isNull(_cursorIndexOfAlarmId)) {
              _tmpAlarmId = null;
            } else {
              _tmpAlarmId = _cursor.getString(_cursorIndexOfAlarmId);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _result = new WakeUpRecord(_tmpId,_tmpDate,_tmpTime,_tmpAlarmLabel,_tmpAlarmId,_tmpTimestamp);
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

  @Override
  public Flow<Integer> getTotalCount() {
    final String _sql = "SELECT COUNT(*) FROM wakeup_records";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"wakeup_records"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Object getAllRecordsForStreak(final Continuation<? super List<WakeUpRecord>> $completion) {
    final String _sql = "SELECT * FROM wakeup_records ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WakeUpRecord>>() {
      @Override
      @NonNull
      public List<WakeUpRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfAlarmLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmLabel");
          final int _cursorIndexOfAlarmId = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<WakeUpRecord> _result = new ArrayList<WakeUpRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WakeUpRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpAlarmLabel;
            if (_cursor.isNull(_cursorIndexOfAlarmLabel)) {
              _tmpAlarmLabel = null;
            } else {
              _tmpAlarmLabel = _cursor.getString(_cursorIndexOfAlarmLabel);
            }
            final String _tmpAlarmId;
            if (_cursor.isNull(_cursorIndexOfAlarmId)) {
              _tmpAlarmId = null;
            } else {
              _tmpAlarmId = _cursor.getString(_cursorIndexOfAlarmId);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new WakeUpRecord(_tmpId,_tmpDate,_tmpTime,_tmpAlarmLabel,_tmpAlarmId,_tmpTimestamp);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
