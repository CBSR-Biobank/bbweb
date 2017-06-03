/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  /**
   *
   */
  function collectionSpecimenDescriptionViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:               '=',
        collectionEventType: '=',
        specimenDescription: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/collection/collectionSpecimenDescriptionView/collectionSpecimenDescriptionView.html',
      controller: CollectionSpecimenDescriptionViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CollectionSpecimenDescriptionViewCtrl.$inject = [
    '$state',
    'gettextCatalog',
    'modalInput',
    'notificationsService',
    'CollectionEventType',
    'CollectionSpecimenDescription',
    'AnatomicalSourceType',
    'PreservationType',
    'PreservationTemperatureType',
    'SpecimenType',
    'stateHelper'
  ];

  function CollectionSpecimenDescriptionViewCtrl($state,
                                                 gettextCatalog,
                                                 modalInput,
                                                 notificationsService,
                                                 CollectionEventType,
                                                 CollectionSpecimenDescription,
                                                 AnatomicalSourceType,
                                                 PreservationType,
                                                 PreservationTemperatureType,
                                                 SpecimenType,
                                                 stateHelper) {
    var vm = this;

    vm.returnState = {
      name: 'home.admin.studies.study.collection.ceventType',
      param: { ceventTypeId: vm.collectionEventType.id }
    };

    vm.editName                    = editName;
    vm.editDescription             = editDescription;
    vm.editAnatomicalSource        = editAnatomicalSource;
    vm.editPreservationType        = editPreservationType;
    vm.editPreservationTemperature = editPreservationTemperature;
    vm.editSpecimenType            = editSpecimenType;
    vm.editUnits                   = editUnits;
    vm.editAmount                  = editAmount;
    vm.editMaxCount                = editMaxCount;
    vm.back                        = back;

    onInit();

    //--

    function onInit() {
      // reload the collection event type in case changes were made to it
      CollectionEventType.get(vm.collectionEventType.studyId, vm.collectionEventType.id)
        .then(function (ceventType) {
          vm.collectionEventType = ceventType;
        });
    }

    function notifySuccess() {
      return notificationsService.success(gettextCatalog.getString('Annotation type changed successfully.'),
                                          gettextCatalog.getString('Change successful'),
                                          1500);
    }

    function updateCollectionEventType() {
      return vm.collectionEventType.updateSpecimenDescription(vm.specimenDescription)
        .then(function (collectionEventType) {
          vm.collectionEventType = collectionEventType;
        })
        .then(notifySuccess)
        .catch(notificationsService.updateError);
    }

    function editName() {
      modalInput.text(gettextCatalog.getString('Specimen spec name'),
                      gettextCatalog.getString('Name'),
                      vm.specimenDescription.name,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.specimenDescription.name = name;
          return updateCollectionEventType()
            .then(function () {
              stateHelper.updateBreadcrumbs();
            });
        });
    }

    function editDescription() {
      modalInput.textArea(gettextCatalog.getString('Specimen spec description'),
                          gettextCatalog.getString('Description'),
                          vm.specimenDescription.description).result
        .then(function (description) {
          vm.specimenDescription.description = description;
          return updateCollectionEventType();
        });
    }

    function editAnatomicalSource() {
      modalInput.select(gettextCatalog.getString('Specimen spec anatomical source'),
                        gettextCatalog.getString('Anatomical source'),
                        vm.specimenDescription.anatomicalSourceType,
                        {
                          required: true,
                          selectOptions: _.values(AnatomicalSourceType)
                        }).result
        .then(function (selection) {
          vm.specimenDescription.anatomicalSourceType = selection;
          return updateCollectionEventType();
        });
    }

    function editPreservationType() {
      modalInput.select(gettextCatalog.getString('Specimen spec preservation type'),
                        gettextCatalog.getString('Preservation type'),
                        vm.specimenDescription.preservationType,
                        {
                          required: true,
                          selectOptions: _.values(PreservationType)
                        }).result
        .then(function (selection) {
          vm.specimenDescription.preservationType = selection;
          return updateCollectionEventType();
        });
    }

    function editPreservationTemperature() {
      modalInput.select(gettextCatalog.getString('Specimen spec preservation temperature'),
                        gettextCatalog.getString('Preservation temperature'),
                        vm.specimenDescription.preservationTemperatureType,
                        {
                          required: true,
                          selectOptions: _.values(PreservationTemperatureType)
                        }).result
        .then(function (selection) {
          vm.specimenDescription.preservationTemperatureType = selection;
          return updateCollectionEventType();
        });
    }

    function editSpecimenType() {
      modalInput.select(gettextCatalog.getString('Specimen spec - specimen type'),
                        gettextCatalog.getString('Sepcimen type'),
                        vm.specimenDescription.specimenType,
                        {
                          required: true,
                          selectOptions: _.values(SpecimenType)
                        }).result
        .then(function (selection) {
          vm.specimenDescription.specimenType = selection;
          return updateCollectionEventType();
        });
    }

    function editUnits() {
      modalInput.text(gettextCatalog.getString('Specimen spec units'),
                      gettextCatalog.getString('Units'),
                      vm.specimenDescription.units,
                      { required: true }).result
        .then(function (units) {
          vm.specimenDescription.units = units;
          return updateCollectionEventType();
        });
    }

    function editAmount() {
      modalInput.positiveFloat(gettextCatalog.getString('Specimen spec amount'),
                               gettextCatalog.getString('Amount'),
                               vm.specimenDescription.amount,
                               { required: true, positiveFloat: true }).result
        .then(function (value) {
          vm.specimenDescription.amount = value;
          return updateCollectionEventType();
        });
    }

    function editMaxCount() {
      modalInput.naturalNumber(gettextCatalog.getString('Specimen spec max count'),
                               gettextCatalog.getString('Max count'),
                               vm.specimenDescription.maxCount,
                               { required: true, naturalNumber: true, min: 1 }).result
        .then(function (value) {
          vm.specimenDescription.maxCount = value;
          return updateCollectionEventType();
        });
    }

    function back() {
      $state.go(vm.returnState.name, vm.returnState.param);
    }

  }

  return collectionSpecimenDescriptionViewDirective;

});
