/**
 * AngularJS Component for {@link domain.participants.Participant Participant} {@link domain.AnnotationType
 * AnnotationType} administration.
 *
 * @namespace admin.studies.components.participantAnnotationTypeView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Controller for this component.
 */
/* @ngInject */
function ParticipantAnnotationTypeViewController($q,
                                                 $state,
                                                 gettextCatalog,
                                                 notificationsService,
                                                 breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //---

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.studies'),
      breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.participants',
        function () { return vm.study.name; }),
      breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.participant.annotationTypeView',
        function () {
          if (_.isUndefined(vm.annotationType)) {
            return gettextCatalog.getString('Error');
          }
          return gettextCatalog.getString(
            'Participant annotation: {{name}}', { name: vm.annotationType.name });
        })
    ];

    vm.onUpdate = onUpdate;
  }

  function onUpdate(attr, annotationType) {
    return vm.study.updateAnnotationType(annotationType)
      .then(postUpdate)
      .catch(notificationsService.updateError)
      .then(notifySuccess)
      .then(() => {
        if (attr === 'name') {
          // reload the state so that the URL gets updated
          $state.go($state.current.name,
                    { annotationTypeSlug: vm.annotationType.slug },
                    { reload: true  })
        }
      });
  }

  function postUpdate(study) {
    vm.study = study;
    vm.annotationType = _.find(vm.study.annotationTypes, { id: vm.annotationType.id });
    if (_.isUndefined(vm.annotationType)) {
      return $q.reject('could not update annotation type');
    }
    return $q.when(true);
  }

  function notifySuccess() {
    return notificationsService.success(
      gettextCatalog.getString('Annotation type changed successfully.'),
      gettextCatalog.getString('Change successful'),
      1500);
  }

}

/**
 * An AngularJS component that allows the user to display an {@link domain.AnnotationType AnnotationType} that
 * belongs to a {@link domain.studies.Study Study} for a {@link domain.participants.Participant Participant}.
 *
 * @memberOf admin.studies.components.participantAnnotationTypeView
 *
 * @param {domain.studies.Study} study - the *Study* the *Annotation Type* belongs to.
 *
 * @param {domain.AnnotationType} annotationType - the *Annotation Type* to display.
 */
const participantAnnotationTypeViewComponent = {
  template: require('./participantAnnotationTypeView.html'),
  controller: ParticipantAnnotationTypeViewController,
  controllerAs: 'vm',
  bindings: {
    study:          '<',
    annotationType: '<'
  }
};

export default ngModule => ngModule.component('participantAnnotationTypeView', participantAnnotationTypeViewComponent)
