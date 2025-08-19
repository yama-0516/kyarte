-- カレンダーイベントテーブルの更新
-- attendeesカラムが存在しない場合は追加
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'calendar_events' AND column_name = 'attendees') THEN
        ALTER TABLE calendar_events ADD COLUMN attendees VARCHAR(255) DEFAULT '';
    END IF;
END $$;

-- is_privateカラムが存在しない場合は追加
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'calendar_events' AND column_name = 'is_private') THEN
        ALTER TABLE calendar_events ADD COLUMN is_private BOOLEAN DEFAULT FALSE;
    END IF;
END $$;

-- 既存レコードのNULL値をデフォルト値で更新（より強力な処理）
UPDATE calendar_events SET attendees = COALESCE(attendees, '') WHERE attendees IS NULL;
UPDATE calendar_events SET is_private = COALESCE(is_private, FALSE) WHERE is_private IS NULL;

-- カラムのデフォルト値を設定（既存のテーブル構造を更新）
ALTER TABLE calendar_events ALTER COLUMN attendees SET DEFAULT '';
ALTER TABLE calendar_events ALTER COLUMN is_private SET DEFAULT FALSE;

-- 制約を追加してNULL値を防ぐ
ALTER TABLE calendar_events ALTER COLUMN attendees SET NOT NULL;
ALTER TABLE calendar_events ALTER COLUMN is_private SET NOT NULL;
