/**
 * AngularJS Component for {@link domain.participants.Participant Participant} {@link domain.AnnotationType
 * AnnotationType} administration.
 *
 * @namespace admin.studies.components.participantAnnotationTypeAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { AnnotationTypeAddController } from '../../../common/controllers/AnnotationTypeAddController'

/*
 * Controller for this component.
 */
class ParticipantAnnotationTypeAddController extends AnnotationTypeAddController {

  constructor($state,
              notificationsService,
              domainNotificationService,
              modalService,
              gettextCatalog) {
    'ngInject'
    super($state,
          notificationsService,
          domainNotificationService,
          modalService,
          gettextCatalog,
          gettextCatalog.getString('Study'),
          'home.admin.studies.study.participants')
  }

  addAnnotationType(annotationType) {
    return this.study.addAnnotationType(annotationType)
  }

}

/**
 * An AngularJS component that allows the user to add a {@link domain.participants.Participant Participant}
 * {@link domain.AnnotationType AnnotationType} to a {@link domain.studies.Study Study}.
 *
 * @memberOf admin.studies.components.participantAnnotationTypeAdd
 *
 * @param {domain.studies.Study} study - the study the *Annotation Type* should be added to to.
 */
const participantAnnotationTypeAddComponent = {
  template: require('./participantAnnotationTypeAdd.html'),
  controller: ParticipantAnnotationTypeAddController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

export default ngModule => ngModule.component('participantAnnotationTypeAdd',
                                             participantAnnotationTypeAddComponent)
