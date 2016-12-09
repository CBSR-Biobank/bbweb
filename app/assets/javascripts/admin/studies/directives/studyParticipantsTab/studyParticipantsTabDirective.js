define(['lodash'], function (_) {
  'use strict';

  /**
   * Displays the participant annotations defined for a study.
   */
  function studyParticipantsTabDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/studyParticipantsTab/studyParticipantsTab.html',
      controller: StudyParticipantsTabCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudyParticipantsTabCtrl.$inject = [
    '$scope',
    '$state',
    'ParticipantAnnotationTypeModals'
  ];

  /**
   * Controller for studyParticipantsTabDirective.
   */
  function StudyParticipantsTabCtrl($scope, $state, ParticipantAnnotationTypeModals) {
    var vm = this;

    _.extend(vm, new ParticipantAnnotationTypeModals());

    // FIXME this is set to empty array for now, but will have to call the correct method in the future
    vm.annotationTypeIdsInUse = [];
    vm.modificationsAllowed = vm.study.isDisabled();

    vm.add = add;
    vm.editAnnotationType = editAnnotationType;
    vm.removeAnnotationType = removeAnnotationType;

    init();

    //--

    function init() {
      // updates the selected tab in 'studyViewDirective' which is the parent directive
      $scope.$emit('tabbed-page-update', 'participants-tab-selected');
    }

    function add() {
      $state.go('home.admin.studies.study.participants.annotationTypeAdd');
    }

    function editAnnotationType(annotationType) {
      $state.go('home.admin.studies.study.participants.annotationTypeView',
                { annotationTypeId: annotationType.uniqueId });
    }

    function removeAnnotationType(annotationType) {
      if (_.includes(vm.annotationTypeIdsInUse, annotationType.uniqueId)) {
        vm.removeInUseModal(annotationType, 'ParticipantAnnotationType');
      } else {
        if (!vm.modificationsAllowed) {
          throw new Error('modifications not allowed');
        }

        vm.remove(annotationType, function () {
          return vm.study.removeAnnotationType(annotationType);
        });
      }

    }
  }


  return studyParticipantsTabDirective;

});
