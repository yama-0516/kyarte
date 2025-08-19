-- カレンダーイベントテーブルの更新
-- 既存レコードのNULL値をデフォルト値で更新
UPDATE calendar_events SET attendees = COALESCE(attendees, '') WHERE attendees IS NULL;
UPDATE calendar_events SET is_private = COALESCE(is_private, FALSE) WHERE is_private IS NULL;

-- カラムのデフォルト値を設定（既存のテーブル構造を更新）
ALTER TABLE calendar_events ALTER COLUMN attendees SET DEFAULT '';
ALTER TABLE calendar_events ALTER COLUMN is_private SET DEFAULT FALSE;
