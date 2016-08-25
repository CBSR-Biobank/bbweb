/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  /**
   *
   */
  function collectionSpecimenSpecAddDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:               '=',
        collectionEventType: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/collection/collectionSpecimenSpecAdd/collectionSpecimenSpecAdd.html',
      controller: CollectionSpecimenSpecAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CollectionSpecimenSpecAddCtrl.$inject = [
    '$state',
    'domainNotificationService',
    'notificationsService',
    'AnatomicalSourceType',
    'PreservationType',
    'PreservationTemperatureType',
    'SpecimenType'
  ];

  var returnState = 'home.admin.studies.study.collection.ceventType';

  function CollectionSpecimenSpecAddCtrl($state,
                                         domainNotificationService,
                                         notificationsService,
                                         AnatomicalSourceType,
                                         PreservationType,
                                         PreservationTemperatureType,
                                         SpecimenType) {
    var vm = this;

    vm.anatomicalSourceTypes = _.values(AnatomicalSourceType);
    vm.preservTypes          = _.values(PreservationType);
    vm.preservTempTypes      = _.values(PreservationTemperatureType);
    vm.specimenTypes         = _.values(SpecimenType);

    vm.submit = submit;
    vm.cancel = cancel;

    //--

    function submit(specimenSpec) {
      vm.collectionEventType.addSpecimenSpec(specimenSpec)
        .then(onAddSuccessful)
        .catch(onAddFailed);

      function onAddSuccessful() {
        notificationsService.submitSuccess();
        $state.go(returnState, {}, { reload: true });
      }

      function onAddFailed(error) {
        return domainNotificationService.updateErrorModal(error, 'study');
      }
    }

    function cancel() {
      $state.go(returnState);
    }
  }

  return collectionSpecimenSpecAddDirective;

});
