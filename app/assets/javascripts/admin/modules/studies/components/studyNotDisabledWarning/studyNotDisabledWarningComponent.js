/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./studyNotDisabledWarning.html'),
  controller: StudyNotDisabledWarningController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

/*
 * Controller for this component.
 */
function StudyNotDisabledWarningController() {

}

export default ngModule => ngModule.component('studyNotDisabledWarning', component)
