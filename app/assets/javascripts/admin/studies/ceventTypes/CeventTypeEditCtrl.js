define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('CeventTypeEditCtrl', CeventTypeEditCtrl);

  CeventTypeEditCtrl.$inject = [
    '$state', 'domainEntityUpdateError', 'ceventTypesService', 'study', 'ceventType', 'annotTypes', 'specimenGroups'
  ];

  /**
   * Used to add or update a collection event type.
   */
  function CeventTypeEditCtrl($state, domainEntityUpdateError, ceventTypesService, study, ceventType, annotTypes, specimenGroups) {
    var action = ceventType.id ? 'Update' : 'Add';

    var vm = this;
    vm.title          =  action + ' Collection Event Type';
    vm.study          = study;
    vm.ceventType     = ceventType;
    vm.annotTypes     = annotTypes;
    vm.specimenGroups = specimenGroups;

    vm.submit              = submit;
    vm.cancel              = cancel;
    vm.addSpecimenGroup    = addSpecimenGroup;
    vm.removeSpecimenGroup = removeSpecimenGroup;
    vm.addAnnotType        = addAnnotType;
    vm.removeAnnotType     = removeAnnotType;

    // used to display the specimen group units label in the form
    vm.specimenGroupsById = _.indexBy(vm.specimenGroups, 'id');

    //---

    function gotoReturnState() {
      return $state.go('admin.studies.study.collection', {}, {reload: true});
    }

    function submit(ceventType) {
      ceventTypesService.addOrUpdate(ceventType)
        .then(gotoReturnState)
        .catch(function(error) {
          domainEntityUpdateError.handleError(
            error,
            'collection event type',
            'admin.studies.study.collection',
            {studyId: study.id},
            {reload: true});
        });
    }

    function cancel() {
      gotoReturnState();
    }

    function addSpecimenGroup() {
      vm.ceventType.specimenGroupData.push({name:'', specimenGroupId:'', maxCount: '', amount: ''});
    }

    function removeSpecimenGroup(sgData) {
      var index = vm.ceventType.specimenGroupData.indexOf(sgData);
      if (index > -1) {
        vm.ceventType.specimenGroupData.splice(index, 1);
      }
    }

    function addAnnotType() {
      vm.ceventType.annotationTypeData.push({name:'', annotationTypeId:'', required: false});
    }

    function removeAnnotType(atData) {
      if (vm.ceventType.annotationTypeData.length < 1) {
        throw new Error('invalid length for annotation type data');
      }

      var index = vm.ceventType.annotationTypeData.indexOf(atData);
      if (index > -1) {
        vm.ceventType.annotationTypeData.splice(index, 1);
      }
    }
  }

});
