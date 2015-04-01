define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function studyAnnotTypesTableDirective() {
    return {
      require: '^tab',
      restrict: 'E',
      scope: {
        study:           '=',
        annotationTypes:      '=',
        annotationTypeIdsInUse: '=',
        annotationTypeName:   '=',
        updateStateName: '=',
        hasRequired:     '@'
      },
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotationTypesPanel.html',
      controller: 'StudyAnnotTypesTableCtrl as vm'
    };
  }

  StudyAnnotTypesTableCtrl.$inject = [
    '$scope',
    '$state',
    'modalService',
    'studyAnnotationTypeUtils',
    'AnnotationTypeViewer',
    'tableService'
  ];

  /**
   * A table to display a study's participant annotation types.
   */
  function StudyAnnotTypesTableCtrl($scope,
                                    $state,
                                    modalService,
                                    studyAnnotationTypeUtils,
                                    AnnotationTypeViewer,
                                    tableService) {
    var vm = this;

    vm.study             = $scope.study;
    vm.annotationTypes        = angular.copy($scope.annotationTypes);
    vm.annotationTypeIdsInUse = $scope.annotationTypeIdsInUse;
    vm.annotationTypeName     = $scope.annotationTypeName;
    vm.updateStateName   = $scope.updateStateName;
    vm.hasRequired       = $scope.hasRequired;
    vm.update            = update;
    vm.remove            = remove;
    vm.information       = information;

    vm.modificationsAllowed = vm.study.isDisabled();

    vm.columns = annotationTypeColumns($scope.annotationTypeName);
    vm.tableParams = tableService.getTableParamsWithCallback(getTableData, {}, { counts: [] });

    //--

    /**
     * Order is important here.
     */
    function annotationTypeColumns(annotationTypeName) {
      var result = [];

      result.push({ title: 'Name', field: 'name',      filter: { 'name':        'text' } });
      result.push({ title: 'Type', field: 'valueType', filter: { 'valueType':   'text' } });

      if (annotationTypeName === 'ParticipantAnnotationType') {
        result.push({ title: 'Required', field: 'required', filter: { 'required': 'text' } });
      }

      result.push({ title: 'Description', field: 'description', filter: { 'description': 'text' } });

      return result;
    }

    function information(annotationType) {
      return new AnnotationTypeViewer(annotationType, vm.annotationTypeName);
    }

    /**
     * Switches state to update a participant annotation type.
     */
    function update(annotationType) {
      if (!vm.study.isDisabled()) {
        throw new Error('study is not disabled');
      }

      if (_.contains(vm.annotationTypeIdsInUse, annotationType.id)) {
        studyAnnotationTypeUtils.inUseModal(annotationType);
      } else {
        $state.go(vm.updateStateName, { annotationTypeId: annotationType.id });
      }
    }

    function remove(annotationType) {
      if (_.contains(vm.annotationTypeIdsInUse, annotationType.id)) {
        studyAnnotationTypeUtils.inUseModal(annotationType);
      } else {
        if (!vm.study.isDisabled()) {
          throw new Error('study is not disabled');
        }
        studyAnnotationTypeUtils.remove(annotationType)
          .then(function () {
            vm.annotationTypes = _.without(vm.annotationTypes, annotationType);
            vm.tableParams.reload();
          });
      }
    }

    function getTableData() {
      return vm.annotationTypes;
    }
  }

  return {
    directive: studyAnnotTypesTableDirective,
    controller: StudyAnnotTypesTableCtrl
  };
});
