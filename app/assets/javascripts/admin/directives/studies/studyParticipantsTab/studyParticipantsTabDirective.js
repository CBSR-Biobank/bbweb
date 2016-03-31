define(['underscore'], function (_) {
  'use strict';

  /**
   *
   */
  function studyParticipantsTabDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '='
      },
      templateUrl : '/assets/javascripts/admin/directives/studies/studyParticipantsTab/studyParticipantsTab.html',
      controller: StudyParticipantsTabCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudyParticipantsTabCtrl.$inject = [
    '$state',
    'studyAnnotationTypeUtils'
  ];

  function StudyParticipantsTabCtrl($state, studyAnnotationTypeUtils) {
    var vm = this;

    // FIXME this is set to empty array for now, but will have to call the correct method in the future
    vm.annotationTypeIdsInUse = [];
    vm.modificationsAllowed = vm.study.isDisabled();

    vm.add = add;
    vm.editAnnotationType = editAnnotationType;
    vm.removeAnnotationType = removeAnnotationType;

    function add() {
      $state.go('home.admin.studies.study.participants.annotationTypeAdd');
    }

    function editAnnotationType(annotationType) {
      $state.go('home.admin.studies.study.participants.annotationTypeView',
                { annotationTypeId: annotationType.uniqueId });
    }

    function removeAnnotationType(annotationType) {
      if (_.contains(vm.annotationTypeIdsInUse, annotationType.uniqueId)) {
        studyAnnotationTypeUtils.removeInUseModal(annotationType, 'ParticipantAnnotationType');
      } else {
        if (!vm.modificationsAllowed) {
          throw new Error('modifications not allowed');
        }

        studyAnnotationTypeUtils.remove(callback, annotationType);
      }

      function callback() {
        return vm.study.removeAnnotationType(annotationType);
      }
    }
  }


  return studyParticipantsTabDirective;

});
