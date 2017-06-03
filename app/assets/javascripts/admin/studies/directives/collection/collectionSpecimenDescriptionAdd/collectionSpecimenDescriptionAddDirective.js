/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  /**
   *
   */
  function collectionSpecimenDescriptionAddDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:               '=',
        collectionEventType: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/collection/collectionSpecimenDescriptionAdd/collectionSpecimenDescriptionAdd.html',
      controller: CollectionSpecimenDescriptionAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CollectionSpecimenDescriptionAddCtrl.$inject = [
    '$state',
    'gettextCatalog',
    'domainNotificationService',
    'notificationsService',
    'AnatomicalSourceType',
    'PreservationType',
    'PreservationTemperatureType',
    'SpecimenType'
  ];

  var returnState = 'home.admin.studies.study.collection.ceventType';

  function CollectionSpecimenDescriptionAddCtrl($state,
                                                gettextCatalog,
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

  return collectionSpecimenDescriptionAddDirective;

});
