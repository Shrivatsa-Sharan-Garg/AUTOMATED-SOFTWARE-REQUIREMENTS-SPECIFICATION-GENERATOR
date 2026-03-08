document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('cards-container');
    let projects = JSON.parse(localStorage.getItem('myProjects')) || [];

    const renderProjects = () => {
        container.innerHTML = '';
        
        if (projects.length === 0) {
            container.innerHTML = '<p style="text-align: center; color: #888; margin-top: 20px;">No projects yet! Click "Create New Project" to get started. 🚀</p>';
            return;
        }

        projects.forEach((proj, index) => {
            const card = document.createElement('div');
            card.className = 'project_card';
            card.innerHTML = `
                <h3>${proj.name}</h3>
                <div class="card-actions">
                    <button onclick="editProject(${index})">Edit SRS</button>
                    <button onclick="previewProject(${index})">Preview SRS</button>
                    <button class="btn-delete" onclick="deleteProject(${index})">Delete</button>
                </div>
            `;
            container.appendChild(card);
        });
    };

    window.createNewProject = () => {
        const name = prompt("Enter Project Name:");
        if (name) {
            projects.push({ name: name });
            localStorage.setItem('myProjects', JSON.stringify(projects));
            renderProjects();
        }
    };

    window.deleteProject = (index) => {
        if (confirm("Are you sure you want to delete this project?")) {
            projects.splice(index, 1); 
            localStorage.setItem('myProjects', JSON.stringify(projects));
            renderProjects();
        }
    };

    window.editProject = (index) => {
        localStorage.setItem('editingIndex', index);
        location.href = '../srs_form/srs_form.html';
    };

    renderProjects();
});