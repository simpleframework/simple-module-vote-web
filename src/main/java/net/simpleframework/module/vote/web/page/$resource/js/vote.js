
function VotePostPage_check(box, max) {
  var cc = box.up(".vi").up();
  if (cc.select("input[type='checkbox']").findAll(function(c) {
    return c.checked;
  }).length > max) {
    box.checked = false;
  }
}

function VotePostPage_post(btn) {
  var act = $Actions['VotePostPage_post']; 
  act.container = btn.up('.VotePostPage');
  act($Form(act.container.down('.vote_c')));
}