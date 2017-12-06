/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

var returnState = 'home.admin.studies.study.collection.ceventType';

/*
 * Controller for this component.
 */
/* @ngInject */
class Controller {

  constructor($state,
              gettextCatalog,
              domainNotificationService,
              notificationsService,
              AnatomicalSourceType,
              PreservationType,
              PreservationTemperatureType,
              SpecimenType,
              breadcrumbService) {
    Object.assign(this,
                  {
                    $state,
                    gettextCatalog,
                    domainNotificationService,
                    notificationsService,
                    AnatomicalSourceType,
                    PreservationType,
                    PreservationTemperatureType,
                    SpecimenType,
                    breadcrumbService
                  });
  }

  $onInit() {
    const studySlug = this.study.slug,
          slug = this.collectionEventType.slug
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.studies'),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.collection.ceventType({ studySlug: "${studySlug}", eventTypeSlug: "${slug}" })`,
        () => this.study.name + ': ' + this.collectionEventType.name),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.collection.ceventType.specimenDescriptionView',
        () => this.gettextCatalog.getString('Add collection specimen'))
    ];

    this.anatomicalSourceTypes = _.values(this.AnatomicalSourceType);
    this.preservTypes          = _.values(this.PreservationType);
    this.preservTempTypes      = _.values(this.PreservationTemperatureType);
    this.specimenTypes         = _.values(this.SpecimenType);
  }

  submit(specimenDescription) {
    this.collectionEventType.addSpecimenDescription(specimenDescription)
      .then(() => {
        this.notificationsService.submitSuccess();
        this.$state.go(returnState, {}, { reload: true });
      })
      .catch((error) =>
             this.domainNotificationService.updateErrorModal(error, this.gettextCatalog.getString('study')));
  }

  cancel() {
    this.$state.go(returnState);
  }
}

const component = {
  template: require('./collectionSpecimenDescriptionAdd.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    collectionEventType: '<'
  }
};

export default ngModule => ngModule.component('collectionSpecimenDescriptionAdd', component)
