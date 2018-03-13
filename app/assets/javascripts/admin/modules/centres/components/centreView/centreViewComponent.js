/**
 * AngularJS Component for {@link domain.centres.Centre Centre} administration.
 *
 * @namespace admin.centres.components.centreView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { TabbedPageController } from '../../../../../common/controllers/TabbedPageController';

/*
 * Controller for this component.
 */
class CentreViewDirective extends TabbedPageController {

  constructor($scope,
              $state,
              gettextCatalog,
              breadcrumbService) {
    'ngInject';
    super(
      [
        {
          heading: gettextCatalog.getString('Summary'),
          sref: 'home.admin.centres.centre.summary',
          active: true
        },
        {
          heading: gettextCatalog.getString('Studies'),
          sref: 'home.admin.centres.centre.studies',
          active: true
        },
        {
          heading: gettextCatalog.getString('Locations'),
          sref: 'home.admin.centres.centre.locations',
          active: true
        },
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
      this.breadcrumbService.forState('home.admin.centres'),
      this.breadcrumbService.forStateWithFunc('home.admin.centres.centre',
                                              () => this.centre.name)
    ];

    this.$scope.$on('centre-name-changed', this.centreNameUpdated.bind(this));
  }

  centreNameUpdated(event, centre) {
    event.stopPropagation();
    this.centre = centre;
  }

}


/**
 * An AngularJS component that displays the {@link domain.centres.Centre Centre} administration page, with a
 * number of tabs. Each tab displays the configuration for a different aspect of the centre.
 *
 * @memberOf admin.centres.components.centreView
 *
 * @param {domain.centres.Centre} centre - the centre to display information for.

 */
const centreViewComponent = {
  template: require('./centreView.html'),
  controller: CentreViewDirective,
  controllerAs: 'vm',
  bindings: {
    centre: '<'
  }
};

export default ngModule => ngModule.component('centreView', centreViewComponent)
