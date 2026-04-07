INSERT INTO srs_docs (project_id, content_json, version) 
VALUES (?, ?, ?)
ON CONFLICT(id) DO UPDATE SET 
    content_json = excluded.content_json,
    last_updated = CURRENT_TIMESTAMP;