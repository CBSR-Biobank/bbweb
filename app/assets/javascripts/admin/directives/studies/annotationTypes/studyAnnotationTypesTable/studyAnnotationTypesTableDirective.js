/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function studyAnnotationTypesTableDirective() {
    var directive = {
      require: '^tab',
      restrict: 'E',
      scope: {},
      bindToController: {
        annotationTypes:        '=',
        annotationTypeIdsInUse: '=',
        annotationTypeName:     '=',
        modificationsAllowed:   '=',
        viewStateName:          '=',
        onRemove:               '&'
      },
      templateUrl: '/assets/javascripts/admin/directives/studies/annotationTypes/studyAnnotationTypesTable/studyAnnotationTypesTable.html',
      controller: StudyAnnotationTypesTableCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudyAnnotationTypesTableCtrl.$inject = [
    '$state',
    'studyAnnotationTypeUtils',
    'AnnotationTypeViewer'
  ];

  /**
   * A table to display a study's participant annotation types.
   */
  function StudyAnnotationTypesTableCtrl($state,
                                         studyAnnotationTypeUtils,
                                         AnnotationTypeViewer) {
    var vm = this;

    vm.update      = update;
    vm.remove      = remove;
    vm.information = information;

    vm.columns = annotationTypeColumns(vm.annotationTypeName);

    //--

    function annotationTypeColumns(annotationTypeName) {
      var result =  [
        { title: 'Name', field: 'name' },
        { title: 'Type', field: 'valueType' },
        { title: 'Required', field: 'required' },
        { title: 'Description', field: 'description' }
      ];
      return result;
    }

    function information(annotationType) {
      return new AnnotationTypeViewer(annotationType, annotationType.name);
    }

    /**
     * Switches state to update a participant annotation type.
     */
    function update(annotationType) {
      if (!vm.modificationsAllowed) {
        throw new Error('modifications not allowed');
      }

      if (_.contains(vm.annotationTypeIdsInUse, annotationType.uniqueId)) {
        studyAnnotationTypeUtils.updateInUseModal(annotationType, vm.annotationTypeName);
      } else {
        $state.go(vm.viewStateName, { annotationTypeId: annotationType.uniqueId });
      }
    }

    function remove(annotationType) {
      if (_.contains(vm.annotationTypeIdsInUse, annotationType.uniqueId)) {
        studyAnnotationTypeUtils.removeInUseModal(annotationType, vm.annotationTypeName);
      } else {
        if (!vm.modificationsAllowed) {
          throw new Error('modifications not allowed');
        }

        studyAnnotationTypeUtils.remove(callback, annotationType);
      }

      function callback() {
        return vm.onRemove()(annotationType);
      }
    }
  }

  return studyAnnotationTypesTableDirective;
});
