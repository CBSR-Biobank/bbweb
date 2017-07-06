/**
 *
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  /**
   * Displays the participant annotations defined for a study.
   */
  var component = {
    templateUrl : '/assets/javascripts/admin/studies/components/studyParticipantsTab/studyParticipantsTab.html',
    controller: StudyParticipantsTabController,
    controllerAs: 'vm',
    bindings: {
      study: '<'
    }
  };

  StudyParticipantsTabController.$inject = [
    '$scope',
    '$state',
    'ParticipantAnnotationTypeModals',
    'notificationsService',
    'gettextCatalog'
  ];

  /*
   * Controller for this component.
   */
  function StudyParticipantsTabController($scope,
                                          $state,
                                          ParticipantAnnotationTypeModals,
                                          notificationsService,
                                          gettextCatalog) {
    var vm = this;

    _.extend(vm, new ParticipantAnnotationTypeModals());

    // FIXME this is set to empty array for now, but will have to call the correct method in the future
    vm.annotationTypeIdsInUse = [];
    vm.modificationsAllowed = vm.study.isDisabled();

    vm.$onInit              = onInit;
    vm.add                  = add;
    vm.editAnnotationType   = editAnnotationType;
    vm.removeAnnotationType = removeAnnotationType;

    //--

    function onInit() {
      // updates the selected tab in 'studyViewDirective' which is the parent directive
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

    function add() {
      $state.go('home.admin.studies.study.participants.annotationTypeAdd');
    }

    function editAnnotationType(annotationType) {
      $state.go('home.admin.studies.study.participants.annotationTypeView',
                { annotationTypeId: annotationType.id });
    }

    function removeAnnotationType(annotationType) {
      if (_.includes(vm.annotationTypeIdsInUse, annotationType.id)) {
        vm.removeInUseModal(annotationType, 'ParticipantAnnotationType');
      } else {
        if (!vm.modificationsAllowed) {
          throw new Error('modifications not allowed');
        }

        vm.remove(annotationType, function () {
          return vm.study.removeAnnotationType(annotationType)
            .then(function () {
              notificationsService.success(gettextCatalog.getString('Annotation removed'));
            });
        });
      }

    }
  }

  return component;
});
