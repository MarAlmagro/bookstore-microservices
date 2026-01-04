document.addEventListener('DOMContentLoaded', function() {
    const flashMessages = document.querySelectorAll('[data-testid^="flash-"]');
    flashMessages.forEach(function(message) {
        setTimeout(function() {
            message.style.transition = 'opacity 0.5s';
            message.style.opacity = '0';
            setTimeout(function() {
                message.remove();
            }, 500);
        }, 5000);
    });

    const deleteButtons = document.querySelectorAll('[data-testid^="book-delete-"]');
    deleteButtons.forEach(function(button) {
        button.addEventListener('click', function(e) {
            if (!confirm('Are you sure you want to delete this item?')) {
                e.preventDefault();
            }
        });
    });
});
