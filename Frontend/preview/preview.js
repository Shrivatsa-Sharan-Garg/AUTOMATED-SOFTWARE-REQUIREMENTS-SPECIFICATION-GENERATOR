document.addEventListener('DOMContentLoaded', () => {
    const index = localStorage.getItem('editingIndex');
    const projects = JSON.parse(localStorage.getItem('myProjects'));

    const srsData = JSON.parse(localStorage.getItem('srsData_' + index)) || {};

    if (projects && projects[index]) {
        document.getElementById('preview-title').innerText = "SRS Preview: " + projects[index].name;
    }

    document.getElementById('disp-purpose').innerText = srsData.purpose || "No data provided.";
    document.getElementById('disp-scope').innerText = srsData.scope || "No data provided.";
    document.getElementById('disp-perspective').innerText = srsData.perspective || "No data provided.";
    document.getElementById('disp-functions').innerText = srsData.functions || "No data provided.";
    document.getElementById('disp-functional').innerText = srsData.functional_requirements || "No data provided.";
    document.getElementById('disp-nonfunctional').innerText = srsData.nonfunctional_requirements || "No data provided.";
});

const saveToLocal = () => {
    const formData = new FormData(document.getElementById('srs-form'));
    const data = Object.fromEntries(formData.entries());
    const index = localStorage.getItem('editingIndex');
    localStorage.setItem('srsData_' + index, JSON.stringify(data));
};