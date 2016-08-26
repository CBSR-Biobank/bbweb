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
    'gettext',
    'modalInput',
    'notificationsService',
    'CollectionSpecimenSpec',
    'AnatomicalSourceType',
    'PreservationType',
    'PreservationTemperatureType',
    'SpecimenType'
  ];

  function CollectionSpecimenSpecViewCtrl($state,
                                          gettext,
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
      return notificationsService.success(gettext('Annotation type changed successfully.'),
                                          gettext('Change successful'),
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
      modalInput.text(gettext('Specimen spec name'),
                      gettext('Name'),
                      vm.specimenSpec.name,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.specimenSpec.name = name;
          return updateCollectionEventType();
        });
    }

    function editDescription() {
      modalInput.textArea(gettext('Specimen spec description'),
                          gettext('Description'),
                          vm.specimenSpec.description).result
        .then(function (description) {
          vm.specimenSpec.description = description;
          return updateCollectionEventType();
        });
    }

    function editAnatomicalSource() {
      modalInput.select(gettext('Specimen spec anatomical source'),
                        gettext('Anatomical source'),
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
      modalInput.select(gettext('Specimen spec preservation type'),
                        gettext('Preservation type'),
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
      modalInput.select(gettext('Specimen spec preservation temperature'),
                        gettext('Preservation temperature'),
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
      modalInput.select(gettext('Specimen spec - specimen type'),
                        gettext('Sepcimen type'),
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
      modalInput.text(gettext('Specimen spec units'),
                      gettext('Units'),
                      vm.specimenSpec.units,
                      { required: true }).result
        .then(function (units) {
          vm.specimenSpec.units = units;
          return updateCollectionEventType();
        });
    }

    function editAmount() {
      modalInput.positiveFloat(gettext('Specimen spec amount'),
                               gettext('Amount'),
                               vm.specimenSpec.amount,
                               { required: true, positiveFloat: true }).result
        .then(function (value) {
          vm.specimenSpec.amount = value;
          return updateCollectionEventType();
        });
    }

    function editMaxCount() {
      modalInput.naturalNumber(gettext('Specimen spec max count'),
                               gettext('Max count'),
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
