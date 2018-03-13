/**
 * AngularJS Component for {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.studyView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { TabbedPageController } from '../../../../../common/controllers/TabbedPageController';

/*
 * Controller for this component.
 */
/* @ngInject */
class StudyViewController extends TabbedPageController {

  constructor($controller,
        $scope,
        $window,
        $state,
        gettextCatalog,
        breadcrumbService) {
    'ngInject';
    super(
      [
        {
          heading: gettextCatalog.getString('Summary'),
          sref: 'home.admin.studies.study.summary',
          active: true
        },
        {
          heading: gettextCatalog.getString('Participants'),
          sref: 'home.admin.studies.study.participants',
          active: false
        },
        {
          heading: gettextCatalog.getString('Collection'),
          sref: 'home.admin.studies.study.collection',
          active: false
        },
        {
          heading: gettextCatalog.getString('Processing'),
          sref: 'home.admin.studies.study.processing',
          active: false
        }
      ],
      0,
      $scope,
      $state);
    Object.assign(this,
                  {
                    gettextCatalog,
                    breadcrumbService
                  });
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.studies'),
      this.breadcrumbService.forStateWithFunc('home.admin.studies.study', () => this.study.name)
    ];

    this.$scope.$on('study-name-changed', this.studyNameUpdated.bind(this));
  }

  studyNameUpdated(event, study) {
    event.stopPropagation();
    this.study = study;
  }
}

/**
 * An AngularJS component that allows the user view and modify the configuration for a {@link
 * domain.studies.Study Study}.
 *
 * Watches for event `study-name-changed` in order to update the name on the study. This event is emitted by
 * child components.
 *
 * @memberOf admin.studies.components.studyView
 *
 * @param {domain.studies.Study} study - the *Study* to view information for.
 */
const studyViewComponent = {
  template: require('./studyView.html'),
  controller: StudyViewController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

export default ngModule => ngModule.component('studyView', studyViewComponent)
