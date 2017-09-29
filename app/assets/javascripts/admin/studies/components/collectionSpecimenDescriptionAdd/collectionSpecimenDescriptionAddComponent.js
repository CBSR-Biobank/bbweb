/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    template: require('./collectionSpecimenDescriptionAdd.html'),
    controller: CollectionSpecimenDescriptionAddController,
    controllerAs: 'vm',
    bindings: {
      study:               '<',
      collectionEventType: '<'
    }
  };

  var returnState = 'home.admin.studies.study.collection.ceventType';

  CollectionSpecimenDescriptionAddController.$inject = [
    '$state',
    'gettextCatalog',
    'domainNotificationService',
    'notificationsService',
    'AnatomicalSourceType',
    'PreservationType',
    'PreservationTemperatureType',
    'SpecimenType'
  ];

  /*
   * Controller for this component.
   */
  function CollectionSpecimenDescriptionAddController($state,
                                                      gettextCatalog,
                                                      domainNotificationService,
                                                      notificationsService,
                                                      AnatomicalSourceType,
                                                      PreservationType,
                                                      PreservationTemperatureType,
                                                      SpecimenType) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.anatomicalSourceTypes = _.values(AnatomicalSourceType);
      vm.preservTypes          = _.values(PreservationType);
      vm.preservTempTypes      = _.values(PreservationTemperatureType);
      vm.specimenTypes         = _.values(SpecimenType);

      vm.submit = submit;
      vm.cancel = cancel;
    }

    function submit(specimenDescription) {
      vm.collectionEventType.addSpecimenDescription(specimenDescription)
        .then(onAddSuccessful)
        .catch(onAddFailed);

      function onAddSuccessful() {
        notificationsService.submitSuccess();
        $state.go(returnState, {}, { reload: true });
      }

      function onAddFailed(error) {
        return domainNotificationService.updateErrorModal(error, gettextCatalog.getString('study'));
      }
    }

    function cancel() {
      $state.go(returnState);
    }
  }

  return component;
});
