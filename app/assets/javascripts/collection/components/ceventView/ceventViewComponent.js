/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

class CeventViewController {

  constructor($scope,
              $state,
              $log,
              gettextCatalog,
              CollectionEventType,
              CollectionEvent,
              Specimen,
              timeService,
              modalService,
              modalInput,
              domainNotificationService,
              notificationsService,
              annotationUpdate,
              resourceErrorService) {
    'ngInject'
    Object.assign(this, {
      $scope,
      $state,
      $log,
      gettextCatalog,
      CollectionEventType,
      CollectionEvent,
      Specimen,
      timeService,
      modalService,
      modalInput,
      domainNotificationService,
      notificationsService,
      annotationUpdate,
      resourceErrorService
    })

    this.annotationLabels = {}
  }

  //--

  $onInit() {
    this.panelOpen = true

    // need to initialize annotations on collection event in the case they have not been assigned
    this.CollectionEventType.get(this.study.id, this.collectionEvent.collectionEventTypeId)
      .then(eventType => {
        this.collectionEventType = eventType;
        this.collectionEvent.setCollectionEventType(this.collectionEventType)
        this.setAnnotationLabels()
      })
  }

  postUpdate(message, title, timeout) {
    return (cevent) => {
      this.collectionEvent = cevent
      this.collectionEvent.setCollectionEventType(this.collectionEventType)
      this.notificationsService.success(message, title, timeout)
    }
  }

  editTimeCompleted() {
    this.modalInput.dateTime(this.gettextCatalog.getString('Update time completed'),
                             this.gettextCatalog.getString('Time completed'),
                             this.collectionEvent.timeCompleted,
                             { required: true })
      .result.then(timeCompleted => {
        this.collectionEvent.updateTimeCompleted(this.timeService.dateAndTimeToUtcString(timeCompleted))
          .then(cevent => {
            this.$scope.$emit('collection-event-updated', cevent)
            this.postUpdate(this.gettextCatalog.getString('Time completed updated successfully.'),
                            this.gettextCatalog.getString('Change successful'),
                            1500)(cevent)
          })
          .catch(this.notificationsService.updateError)
      })
  }

  editAnnotation(annotation) {
    this.annotationUpdate.update(annotation, 'Update ' + annotation.getLabel())
      .then(newAnnotation => {
        this.collectionEvent.addAnnotation(newAnnotation)
          .then(this.postUpdate(this.gettextCatalog.getString('Annotation updated successfully.'),
                                this.gettextCatalog.getString('Change successful'),
                                1500))
          .catch(this.notificationsService.updateError)
      })
  }

  panelButtonClicked() {
    this.panelOpen = !this.panelOpen
  }

  remove() {
    this.Specimen.list(this.collectionEvent.id)
      .then(pagedResult => {
        if (pagedResult.items.length > 0) {
          this.modalService.modalOk(
            this.gettextCatalog.getString('Cannot remove collection event'),
            this.gettextCatalog.getString('This collection event has specimens. Please remove the specimens first.'))
        } else {
          const promiseFn = () => this.collectionEvent.remove()
                .then(() => {
                  this.notificationsService.success(this.gettextCatalog.getString('Collection event removed'))
                  this.$state.go('home.collection.study.participant.cevents', {}, { reload: true })
                })

          this.domainNotificationService.removeEntity(
            promiseFn,
            this.gettextCatalog.getString('Remove event'),
            /// visit number comes from the collection event
            this.gettextCatalog.getString(
              'Are you sure you want to remove event with visit # <strong>{{visitNumber}}</strong>?',
              { visitNumber: this.collectionEvent.visitNumber}),
            this.gettextCatalog.getString('Remove failed'),
            this.gettextCatalog.getString(
              'Collection event with visit number {{visitNumber}} cannot be removed',
              { visitNumber: this.collectionEvent.visitNumber}))
        }
      })
  }

  setAnnotationLabels() {
    this.annotationLabels = []
    this.collectionEvent.annotations.forEach(annotation => {
      this.annotationLabels[annotation.annotationTypeId] = annotation.getLabel()
    })
  }

}

var component = {
  template: require('./ceventView.html'),
  controller: CeventViewController,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    participant:         '<',
    collectionEvent:     '<'
  }
}

export default ngModule => ngModule.component('ceventView', component)
