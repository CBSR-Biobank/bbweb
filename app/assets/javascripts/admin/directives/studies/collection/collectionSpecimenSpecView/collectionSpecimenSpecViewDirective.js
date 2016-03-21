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
    'modalInput',
    'notificationsService',
    'CollectionSpecimenSpec',
    'AnatomicalSourceType',
    'PreservationType',
    'PreservationTemperatureType',
    'SpecimenType'
  ];

  function CollectionSpecimenSpecViewCtrl($state,
                                          modalInput,
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
      modalInput.text('Specimen spec name',
                      'Name',
                      vm.specimenSpec.name,
                      { required: true, minLength: 2 })
        .then(function (name) {
          vm.specimenSpec.name = name;
          return updateCollectionEventType;
        });
    }

    function editDescription() {
      modalInput.textArea('Specimen spec description',
                          'Description',
                          vm.specimenSpec.description)
        .then(function (description) {
          vm.specimenSpec.description = description;
          return updateCollectionEventType;
        });
    }

    function editAnatomicalSource() {
      modalInput.select('Specimen spec anatomical source',
                        'Anatomical source',
                        vm.specimenSpec.anatomicalSourceType,
                        {
                          required: true,
                          selectOptions: AnatomicalSourceType.values()
                        })
        .then(function (selection) {
          vm.specimenSpec.anatomicalSourceType = selection;
          return updateCollectionEventType;
        });
    }

    function editPreservationType() {
      modalInput.select('Specimen spec preservation type',
                        'Preservation type',
                        vm.specimenSpec.preservationType,
                        {
                          required: true,
                          selectOptions: PreservationType.values()
                        })
        .then(function (selection) {
          vm.specimenSpec.preservationType = selection;
          return updateCollectionEventType;
        });
    }

    function editPreservationTemperature() {
      modalInput.select('Specimen spec preservation temperature',
                        'Preservation temperature',
                        vm.specimenSpec.preservationTemperatureType,
                        {
                          required: true,
                          selectOptions: PreservationTemperatureType.values()
                        })
        .then(function (selection) {
          vm.specimenSpec.preservationTemperatureType = selection;
          return updateCollectionEventType;
        });
    }

    function editSpecimenType() {
      modalInput.select('Specimen spec - specimen type',
                        'Sepcimen type',
                        vm.specimenSpec.specimenType,
                        {
                          required: true,
                          selectOptions: SpecimenType.values()
                        })
        .then(function (selection) {
          vm.specimenSpec.specimenType = selection;
          return updateCollectionEventType;
        });
    }

    function editUnits() {
      modalInput.text('Specimen spec units', 'Units', vm.specimenSpec.units, { required: true })
        .then(function (units) {
          vm.specimenSpec.units = units;
          return updateCollectionEventType;
        });
    }

    function editAmount() {
      modalInput.positiveFloat('Specimen spec amount', 'Amount', vm.specimenSpec.amount, { required: true })
        .then(function (value) {
          vm.specimenSpec.amount = value;
          return updateCollectionEventType;
        });
    }

    function editMaxCount() {
      modalInput.naturalNumber('Specimen spec max count',
                               'Max count',
                               vm.specimenSpec.maxCount,
                               { required: true })
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
