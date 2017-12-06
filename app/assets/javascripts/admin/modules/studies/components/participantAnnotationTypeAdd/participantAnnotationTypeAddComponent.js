/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
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

const component = {
  template: require('./participantAnnotationTypeAdd.html'),
  controller: ParticipantAnnotationTypeAddController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

export default ngModule => ngModule.component('participantAnnotationTypeAdd', component)
