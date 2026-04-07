document.addEventListener("DOMContentLoaded", async () => {
  const index = localStorage.getItem("editingIndex");
  const projects = JSON.parse(localStorage.getItem("myProjects")) || [];
  const container = document.getElementById("preview_content");

  if (projects[index]) {
    document.getElementById("preview-title").innerText =
      `SRS Preview: ${projects[index].name}`;

    try {
      const res = await fetch(
        `http://localhost:8080/api/get-srs?project_id=${projects[index].id}`,
      );
      const data = await res.json();

      if (data.content_json) {
        renderPreview(data.content_json);
      }
    } catch (e) {
      container.innerHTML = "<p>Error loading data from server.</p>";
    }
  }

  function renderPreview(content) {
    container.innerHTML = "";
    for (const [key, value] of Object.entries(content)) {
      const sectionHeader = document.createElement("h3");
      sectionHeader.innerText = key.replace(/_/g, " ").replace(/\./g, " ");

      const text = document.createElement("p");
      text.innerText = value || "Not specified.";
      text.className = "srs-text";

      container.appendChild(sectionHeader);
      container.appendChild(text);
    }
  }
});

function downloadAsDoc() {
  const header =
    "<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'><head><meta charset='utf-8'><title>SRS Document</title></head><body>";
  const footer = "</body></html>";
  const sourceHTML =
    header + document.getElementById("preview_content").innerHTML + footer;

  const source =
    "data:application/vnd.ms-word;charset=utf-8," +
    encodeURIComponent(sourceHTML);
  const fileDownload = document.createElement("a");
  document.body.appendChild(fileDownload);
  fileDownload.href = source;
  fileDownload.download = "SRS_Document.doc";
  fileDownload.click();
  document.body.removeChild(fileDownload);
}
