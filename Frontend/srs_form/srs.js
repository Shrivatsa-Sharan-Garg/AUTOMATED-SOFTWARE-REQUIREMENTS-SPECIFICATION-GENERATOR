document.addEventListener('DOMContentLoaded', () => {
    const index = localStorage.getItem('editingIndex');
    const projects = JSON.parse(localStorage.getItem('myProjects'));

    if (index !== null && projects[index]) {
        const projectName = projects[index].name;
        document.getElementById('srs-title').innerText = `Edit Software Requirements Specification for ${projectName}`;
    }
});

document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('form');
    const status = document.getElementById('save-status');
    let timeout = null;

    const autoSave = () => {
        status.innerText = "Saving... 💾";
        
        const formData = new FormData(form);
        
        fetch('/save_srs', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if(response.ok) {
                status.innerText = "All changes saved.";
            }
        })
        .catch(err => status.innerText = "Save failed.");
    };

    form.addEventListener('input', () => {
        status.innerText = "Typing...";
        clearTimeout(timeout);
        timeout = setTimeout(autoSave, 1500); 
    });
});