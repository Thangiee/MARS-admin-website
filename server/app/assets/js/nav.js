$('a#menu-item').click(function() {
  $('li#menu-item-container').removeClass().addClass("no-padding");
  var parent = $(event.target).parent();
  // if clicked the icon, pass on the click event to the menu item
  if (parent[0].id == "menu-item") {
    parent[0].click();
  } else {
    parent.removeClass().addClass('no-padding active');
  }
});

$('.button-collapse').sideNav({
    menuWidth: 320, // Default is 240
    edge: 'left' // Choose the horizontal origin
  }
);

