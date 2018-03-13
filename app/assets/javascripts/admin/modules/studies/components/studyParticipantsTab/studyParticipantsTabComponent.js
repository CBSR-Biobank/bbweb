/**
 * AngularJS Component for {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.studyParticipantsTab
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Controller for this component.
 */
/* @ngInject */
function StudyParticipantsTabController($scope,
                                        $state,
                                        ParticipantAnnotationTypeRemove,
                                        notificationsService,
                                        gettextCatalog) {
  var vm = this;
  vm.$onInit = onInit;
  vm.annotationTypeRemove = new ParticipantAnnotationTypeRemove();

  //--

  function onInit() {
    // FIXME this is set to empty array for now, but will have to call the correct method in the future
    vm.annotationTypeIdsInUse = [];
    vm.modificationsAllowed = vm.study.isDisabled();

    vm.$onInit              = onInit;
    vm.add                  = add;
    vm.editAnnotationType   = editAnnotationType;
    vm.removeAnnotationType = removeAnnotationType;

    // updates the selected tab in 'studyViewDirective' which is the parent directive
    $scope.$emit('tabbed-page-update', 'tab-selected');
  }

  function add() {
    $state.go('home.admin.studies.study.participants.annotationTypeAdd');
  }

  function editAnnotationType(annotationType) {
    $state.go('home.admin.studies.study.participants.annotationTypeView',
              { annotationTypeSlug: annotationType.slug });
  }

  function removeAnnotationType(annotationType) {
    if (_.includes(vm.annotationTypeIdsInUse, annotationType.id)) {
      vm.annotationTypeRemove.removeInUseModal(annotationType, 'ParticipantAnnotationType');
    } else {
      if (!vm.modificationsAllowed) {
        throw new Error('modifications not allowed');
      }

      vm.annotationTypeRemove.remove(
        annotationType,
        () => vm.study.removeAnnotationType(annotationType)
          .then(() =>
                notificationsService.success(gettextCatalog.getString('Annotation removed'))));
    }

  }
}

/**
 * An AngularJS component that displays the {@link domain.participants.Participant Participants} related
 * configuration for a {@link domain.studies.Study Study}.
 *
 * @memberOf admin.studies.components.studyParticipantsTab
 *
 * @param {domain.studies.Study} study - the study to display information for.
 */
const studyParticipantsTabComponent = {
  template: require('./studyParticipantsTab.html'),
  controller: StudyParticipantsTabController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

export default ngModule => ngModule.component('studyParticipantsTab', studyParticipantsTabComponent)
