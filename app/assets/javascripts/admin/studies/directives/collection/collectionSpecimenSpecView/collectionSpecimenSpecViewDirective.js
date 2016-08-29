/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
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
      templateUrl : '/assets/javascripts/admin/studies/directives/collection/collectionSpecimenSpecView/collectionSpecimenSpecView.html',
      controller: CollectionSpecimenSpecViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CollectionSpecimenSpecViewCtrl.$inject = [
    '$state',
    'gettextCatalog',
    'modalInput',
    'notificationsService',
    'CollectionSpecimenSpec',
    'AnatomicalSourceType',
    'PreservationType',
    'PreservationTemperatureType',
    'SpecimenType'
  ];

  function CollectionSpecimenSpecViewCtrl($state,
                                          gettextCatalog,
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

    function notifySuccess() {
      return notificationsService.success(gettextCatalog.getString('Annotation type changed successfully.'),
                                          gettextCatalog.getString('Change successful'),
                                          1500);
    }

    function updateCollectionEventType() {
      return vm.collectionEventType.updateSpecimenSpec(vm.specimenSpec)
        .then(function (collectionEventType) {
          vm.collectionEventType = collectionEventType;
        })
        .then(notifySuccess)
        .catch(notificationsService.updateError);
    }

    function editName() {
      modalInput.text(gettextCatalog.getString('Specimen spec name'),
                      gettextCatalog.getString('Name'),
                      vm.specimenSpec.name,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.specimenSpec.name = name;
          return updateCollectionEventType();
        });
    }

    function editDescription() {
      modalInput.textArea(gettextCatalog.getString('Specimen spec description'),
                          gettextCatalog.getString('Description'),
                          vm.specimenSpec.description).result
        .then(function (description) {
          vm.specimenSpec.description = description;
          return updateCollectionEventType();
        });
    }

    function editAnatomicalSource() {
      modalInput.select(gettextCatalog.getString('Specimen spec anatomical source'),
                        gettextCatalog.getString('Anatomical source'),
                        vm.specimenSpec.anatomicalSourceType,
                        {
                          required: true,
                          selectOptions: _.values(AnatomicalSourceType)
                        }).result
        .then(function (selection) {
          vm.specimenSpec.anatomicalSourceType = selection;
          return updateCollectionEventType();
        });
    }

    function editPreservationType() {
      modalInput.select(gettextCatalog.getString('Specimen spec preservation type'),
                        gettextCatalog.getString('Preservation type'),
                        vm.specimenSpec.preservationType,
                        {
                          required: true,
                          selectOptions: _.values(PreservationType)
                        }).result
        .then(function (selection) {
          vm.specimenSpec.preservationType = selection;
          return updateCollectionEventType();
        });
    }

    function editPreservationTemperature() {
      modalInput.select(gettextCatalog.getString('Specimen spec preservation temperature'),
                        gettextCatalog.getString('Preservation temperature'),
                        vm.specimenSpec.preservationTemperatureType,
                        {
                          required: true,
                          selectOptions: _.values(PreservationTemperatureType)
                        }).result
        .then(function (selection) {
          vm.specimenSpec.preservationTemperatureType = selection;
          return updateCollectionEventType();
        });
    }

    function editSpecimenType() {
      modalInput.select(gettextCatalog.getString('Specimen spec - specimen type'),
                        gettextCatalog.getString('Sepcimen type'),
                        vm.specimenSpec.specimenType,
                        {
                          required: true,
                          selectOptions: _.values(SpecimenType)
                        }).result
        .then(function (selection) {
          vm.specimenSpec.specimenType = selection;
          return updateCollectionEventType();
        });
    }

    function editUnits() {
      modalInput.text(gettextCatalog.getString('Specimen spec units'),
                      gettextCatalog.getString('Units'),
                      vm.specimenSpec.units,
                      { required: true }).result
        .then(function (units) {
          vm.specimenSpec.units = units;
          return updateCollectionEventType();
        });
    }

    function editAmount() {
      modalInput.positiveFloat(gettextCatalog.getString('Specimen spec amount'),
                               gettextCatalog.getString('Amount'),
                               vm.specimenSpec.amount,
                               { required: true, positiveFloat: true }).result
        .then(function (value) {
          vm.specimenSpec.amount = value;
          return updateCollectionEventType();
        });
    }

    function editMaxCount() {
      modalInput.naturalNumber(gettextCatalog.getString('Specimen spec max count'),
                               gettextCatalog.getString('Max count'),
                               vm.specimenSpec.maxCount,
                               { required: true, naturalNumber: true, min: 1 }).result
        .then(function (value) {
          vm.specimenSpec.maxCount = value;
          return updateCollectionEventType();
        });
    }

    function back() {
      $state.go(vm.returnState.name, vm.returnState.param);
    }

  }

  return collectionSpecimenSpecViewDirective;

});
