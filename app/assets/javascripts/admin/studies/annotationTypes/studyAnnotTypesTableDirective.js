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
        annotTypes:      '=',
        annotTypeIdsInUse: '=',
        annotTypeName:   '=',
        updateStateName: '=',
        hasRequired:     '@'
      },
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypesPanel.html',
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
    vm.annotTypes        = angular.copy($scope.annotTypes);
    vm.annotTypeIdsInUse = $scope.annotTypeIdsInUse;
    vm.annotTypeName     = $scope.annotTypeName;
    vm.updateStateName   = $scope.updateStateName;
    vm.hasRequired       = $scope.hasRequired;
    vm.update            = update;
    vm.remove            = remove;
    vm.information       = information;

    vm.modificationsAllowed = vm.study.isDisabled();

    vm.columns = annotationTypeColumns($scope.annotTypeName);
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
      return new AnnotationTypeViewer(annotationType, vm.annotTypeName);
    }

    /**
     * Switches state to update a participant annotation type.
     */
    function update(annotType) {
      if (!vm.study.isDisabled()) {
        throw new Error('study is not disabled');
      }

      if (_.contains(vm.annotTypeIdsInUse, annotType.id)) {
        studyAnnotationTypeUtils.inUseModal(annotType);
      } else {
        $state.go(vm.updateStateName, { annotTypeId: annotType.id });
      }
    }

    function remove(annotType) {
      if (_.contains(vm.annotTypeIdsInUse, annotType.id)) {
        studyAnnotationTypeUtils.inUseModal(annotType);
      } else {
        if (!vm.study.isDisabled()) {
          throw new Error('study is not disabled');
        }
        studyAnnotationTypeUtils.remove(annotType)
          .then(function () {
            vm.annotTypes = _.without(vm.annotTypes, annotType);
            vm.tableParams.reload();
          });
      }
    }

    function getTableData() {
      return vm.annotTypes;
    }
  }

  return {
    directive: studyAnnotTypesTableDirective,
    controller: StudyAnnotTypesTableCtrl
  };
});
