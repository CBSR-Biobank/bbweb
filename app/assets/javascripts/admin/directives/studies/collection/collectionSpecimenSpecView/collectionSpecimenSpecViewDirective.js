/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function collectionSpecimenSpecViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:               '=',
        collectionEventType: '=',
        specimenSpec:        '='
      },
      templateUrl : '/assets/javascripts/admin/directives/studies/collection/collectionSpecimenSpecView/collectionSpecimenSpecView.html',
      controller: CollectionSpecimenSpecViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CollectionSpecimenSpecViewCtrl.$inject = [
    '$state',
    'modalService',
    'notificationsService',
    'CollectionSpecimenSpec',
    'AnatomicalSourceType',
    'PreservationType',
    'PreservationTemperatureType',
    'SpecimenType'
  ];

  function CollectionSpecimenSpecViewCtrl($state,
                                          modalService,
                                          notificationsService,
                                          CollectionSpecimenSpec,
                                          AnatomicalSourceType,
                                          PreservationType,
                                          PreservationTemperatureType,
                                          SpecimenType) {
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


    //--

    function updateCollectionEventType() {
      vm.collectionEventType.updateSpecimenSpec(vm.specimenSpec)
        .then(function (collectionEventType) {
          vm.collectionEventType = collectionEventType;
        });
    }

    function editName() {
      modalService.modalTextInput('Specimen spec name',
                                  'Name',
                                  vm.specimenSpec.name)
        .then(function (name) {
          vm.specimenSpec.name = name;
          return updateCollectionEventType;
        });
    }

    function editDescription() {
      modalService.modalTextAreaInput('Specimen spec description',
                                      'Description',
                                      vm.specimenSpec.description)
        .then(function (description) {
          vm.specimenSpec.description = description;
          return updateCollectionEventType;
        });
    }

    function editAnatomicalSource() {
      modalService.modalRequiredSelect('Specimen spec anatomical source',
                                       'Anatomical source',
                                       vm.specimenSpec.anatomicalSourceType,
                                       AnatomicalSourceType.values())
        .then(function (selection) {
          vm.specimenSpec.anatomicalSourceType = selection;
          return updateCollectionEventType;
        });
    }

    function editPreservationType() {
      modalService.modalRequiredSelect('Specimen spec preservation type',
                                       'Preservation type',
                                       vm.specimenSpec.preservationType,
                                       PreservationType.values())
        .then(function (selection) {
          vm.specimenSpec.preservationType = selection;
          return updateCollectionEventType;
        });
    }

    function editPreservationTemperature() {
      modalService.modalRequiredSelect('Specimen spec preservation temperature',
                                       'Preservation temperature',
                                       vm.specimenSpec.preservationTemperatureType,
                                       PreservationTemperatureType.values())
        .then(function (selection) {
          vm.specimenSpec.preservationTemperatureType = selection;
          return updateCollectionEventType;
        });
    }

    function editSpecimenType() {
      modalService.modalRequiredSelect('Specimen spec - specimen type',
                                       'Sepcimen type',
                                       vm.specimenSpec.specimenType,
                                       SpecimenType.values())
        .then(function (selection) {
          vm.specimenSpec.specimenType = selection;
          return updateCollectionEventType;
        });
    }

    function editUnits() {
      modalService.modalTextInput('Specimen spec units',
                                  'Units',
                                  vm.specimenSpec.units)
        .then(function (units) {
          vm.specimenSpec.units = units;
          return updateCollectionEventType;
        });
    }

    function editAmount() {
      modalService.modalRequiredPositiveFloat('Specimen spec amount',
                                              'Amount',
                                              vm.specimenSpec.amount)
        .then(function (value) {
          vm.specimenSpec.amount = value;
          return updateCollectionEventType;
        });
    }

    function editMaxCount() {
      modalService.modalRequiredNaturalNumber('Specimen spec max count',
                                              'Max count',
                                              vm.specimenSpec.maxCount)
        .then(function (value) {
          vm.specimenSpec.maxCount = value;
          return updateCollectionEventType;
        });
    }

    function back() {
      $state.go(vm.returnState.name, vm.returnState.param);
    }

  }

  return collectionSpecimenSpecViewDirective;

});
