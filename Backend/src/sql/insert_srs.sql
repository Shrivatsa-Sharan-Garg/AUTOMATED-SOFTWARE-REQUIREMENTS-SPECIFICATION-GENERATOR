INSERT INTO srs_docs (project_id, content_json, version) 
VALUES (?, ?, ?)
ON CONFLICT(project_id) DO UPDATE SET 
    content_json = excluded.content_json,
    version = excluded.version,
    last_updated = CURRENT_TIMESTAMP;