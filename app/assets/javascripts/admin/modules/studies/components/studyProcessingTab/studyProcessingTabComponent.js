/**
 * AngularJS Component for {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.studyProcessingTab
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

class StudyProcessingTabController {

  constructor($scope,
              ProcessingType) {
    'ngInject';
    Object.assign(this,
                  {
                    $scope,
                    ProcessingType
                  });
  }

  $onInit() {
    // updates the selected tab in 'studyViewDirective' which is the parent component
    this.$scope.$emit('tabbed-page-update', 'tab-selected');

    this.ProcessingType.list(this.study.slug)
      .then(reply => {
        this.hasProcessingTypes = (reply.total > 0);
      });
  }

}

/**
 * An AngularJS component that displays the {@link domain.studies.ProcessingType ProcessingTypes} configured
 * for a {@link domain.studies.Study Study},
 *
 * @memberOf admin.studies.components.studyProcessingTab
 *
 * @param {domain.studies.Study} study - the *Study* to view *Processing Types* for.
 */
const studyProcessingTabComponent = {
  template: require('./studyProcessingTab.html'),
  controller: StudyProcessingTabController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

function stateConfig($stateProvider, $urlRouterProvider) {
  'ngInject';
  $stateProvider.state('home.admin.studies.study.processing', {
    url: '/processing',
    views: {
      'studyDetails': 'studyProcessingTab'
    }
  });
  $urlRouterProvider.otherwise('/');
}

export default ngModule => {
  ngModule
    .config(stateConfig)
    .component('studyProcessingTab', studyProcessingTabComponent)
}
