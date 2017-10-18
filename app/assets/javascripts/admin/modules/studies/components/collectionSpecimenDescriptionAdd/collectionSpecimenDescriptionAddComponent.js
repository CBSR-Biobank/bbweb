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
              SpecimenType) {
    Object.assign(this,
                  {
                    $state,
                    gettextCatalog,
                    domainNotificationService,
                    notificationsService,
                    AnatomicalSourceType,
                    PreservationType,
                    PreservationTemperatureType,
                    SpecimenType
                  });
  }

  $onInit() {
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
