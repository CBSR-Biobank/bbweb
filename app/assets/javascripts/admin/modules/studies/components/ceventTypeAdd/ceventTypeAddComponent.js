/**
 * AngularJS Component for {@link domain.studies.CollectionEventType CollectionEventType} administration.
 *
 * @namespace admin.studies.components.ceventTypeAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

class CeventTypeAddController {

  constructor($state,
              gettextCatalog,
              CollectionEventType,
              breadcrumbService,
              domainNotificationService,
              notificationsService,
              modalService) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    gettextCatalog,
                    CollectionEventType,
                    breadcrumbService,
                    domainNotificationService,
                    notificationsService,
                    modalService
                  });
  }

  $onInit() {
    this.ceventType = new this.CollectionEventType({}, { study: this.study });

    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.studies'),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.collection({ studySlug: "${this.study.slug}" })`,
        () => this.study.name),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.collection.ceventTypeAdd',
        () => this.gettextCatalog.getString('Add collection event'))
    ];
  }

  submit() {
    this.ceventType.add()
      .then(() => {
        this.notificationsService.submitSuccess();
        return this.$state.go('^', {}, { reload: true });
      })
      .catch(error => {
        if ((typeof error.message === 'string') && (error.message.indexOf('name already exists') > -1)) {
          this.modalService.modalOk(
            this.gettextCatalog.getString('Cannot Add'),
            this.gettextCatalog.getString(
              'The name <b>{{name}}</b> has already been used. Please use another name.',
              { name : this.ceventType.name }));
        } else {
          this.domainNotificationService.updateErrorModal(
            error,
            this.gettextCatalog.getString('collection event type'));
        }
    });
  }

  cancel() {
    return this.$state.go('^');
  }

}

/**
 * An AngularJS component that allows the user to add a {@link domain.studies.CollectionEventType
 * CollectionEventType} using an HTML form.
 *
 * @memberOf admin.studies.components.ceventTypeAdd
 *
 * @param {domain.studies.Study} study - the study to add the collection event type to.
 */
const ceventTypeAddComponent = {
  template: require('./ceventTypeAdd.html'),
  controller: CeventTypeAddController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

export default ngModule => ngModule.component('ceventTypeAdd', ceventTypeAddComponent)
