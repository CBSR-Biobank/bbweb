/**
 * AngularJS Component for {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.studyNotDisabledWarning
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
function StudyNotDisabledWarningController() {

}

/**
 * An AngularJS component that displays a warning on the page stating that the {@link domain.studies.Study
 * Study} is not disabled.
 *
 * @memberOf admin.studies.components.studyNotDisabledWarning
 *
 * @param {domain.studies.Study} study - the *Study* the that is not disabled.
 */
const studyNotDisabledWarningComponent = {
  template: require('./studyNotDisabledWarning.html'),
  controller: StudyNotDisabledWarningController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

export default ngModule => ngModule.component('studyNotDisabledWarning', studyNotDisabledWarningComponent)
