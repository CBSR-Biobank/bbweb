/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
class UserMembershipController {

  constructor($q,
              $log,
              asyncInputModal,
              gettextCatalog,
              domainNotificationService,
              notificationsService,
              modalService,
              MembershipName,
              UserName,
              matchingMembershipNames,
              matchingUserNames) {
    'ngInject'
    Object.assign(
      this,
      {
        $q,
        $log,
        asyncInputModal,
        gettextCatalog,
        domainNotificationService,
        notificationsService,
        modalService,
        MembershipName,
        UserName,
        matchingMembershipNames,
        matchingUserNames
      })
  }

  $onInit() {
    this.updateLabels();
  }

  $onChanges(changed) {
    if (changed.user) {
      this.user = changed.user.currentValue;
      this.updateLabels();
    }
  }

  updateLabels() {
    this.membershipNameLabels = this.membershipToLabel(this.user.membership)
  }

  membershipToLabel(membership) {
    return {
      label:   membership.name,
      tooltip: this.gettextCatalog.getString('Remove ' + membership.name),
      obj:     membership
    }
  }

  addMembership() {
    this.matchingMembershipNames.open(this.gettextCatalog.getString('Add a membership'),
                                this.gettextCatalog.getString('Membership'),
                                this.gettextCatalog.getString('enter a membership\'s name or partial name'),
                                this.gettextCatalog.getString('No matching membership found'),
                                this.user.membership)
      .then((modalValue) => {
        this.onMembershipAddRequest()(modalValue.obj.id)
      })
      .catch((error) => {
        this.$log.error(error);
      });
  }

  membershipLabelSelected(membershipName) {
    const promiseFn = () => {
      this.onMembershipRemoveRequest()(membershipName)
        .then(() => {
          this.notificationsService.success(
            this.gettextCatalog.getString('Membership removed: {{name}}', { name: membershipName.name }))
        })
        .catch((error) => {
          this.$log.error(error);
        });
    }

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove membership from user'),
      this.gettextCatalog.getString(
        'Are you sure you want to remove the membership named <strong>{{name}}</strong> from this user?',
        { name: membershipName.name }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString('The membership named {{name}} cannot be removed', { name: membershipName.name }))

  }
}

/*
 * Allows the logged in user to modify another user's membership.
 */
var userMembership = {
  template: require('./userMembership.html'),
  controller: UserMembershipController,
  controllerAs: 'vm',
  bindings: {
    user:                '<',
    onMembershipAddRequest:    '&',
    onMembershipRemoveRequest: '&',
    onMembershipCopyRequest:   '&'
  }
};

export default ngModule => ngModule.component('userMembership', userMembership)
