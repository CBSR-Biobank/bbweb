/**
 * AngularJS Component for {@link domain.users.User User} administration.
 *
 * @namespace admin.users.components.userMembership
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
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
    if (this.user.membership) {
      this.membershipNameLabels = [ this.membershipToLabel(this.user.membership) ];
    } else {
      this.membershipNameLabels = [];
    }
  }

  membershipToLabel(membership) {
    return {
      label:   membership.name,
      tooltip: this.gettextCatalog.getString('Remove membership ' + membership.name),
      obj:     membership
    }
  }

  addMembership() {
    let membershipLabel;
    this.matchingMembershipNames
      .open(this.gettextCatalog.getString('Add a membership'),
            this.gettextCatalog.getString('Membership'),
            this.gettextCatalog.getString('enter a membership\'s name or partial name'),
            this.gettextCatalog.getString('No matching membership found'),
            this.user.membership)
      .then(modalValue => {
        membershipLabel = modalValue.label;
        return this.onMembershipAddRequest()(modalValue.obj.id);
      })
      .then(() => {
        this.notificationsService.success(
          this.gettextCatalog.getString('Membership added: {{name}}', { name: membershipLabel }))
      })
      .catch((error) => {
        this.$log.error(error);
      });
  }

  membershipLabelSelected(membershipName) {
    const promiseFn = () => {
      this.onRemoveRequest()(membershipName)
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

/**
 * An AngularJS component that allows the logged in user to modify another {@link domain.users.user
 * User's} {@link domain.access.Membership Membership}.
 *
 * @memberOf admin.users.components.userMembership
 *
 * @param {domain.users.User} user - the user to modify the membership for.
 *
 * @param {admin.users.components.userMembership.onAddRequest} onAddRequest - the function this
 * component calls to request  a new membership be added.
 *
 * @param {admin.users.components.userMembership.onRemoveRequest} onRemoveRequest - the function
 * this component calls to request removal of a membership.
 */
const userMembershipComponent = {
  template: require('./userMembership.html'),
  controller: UserMembershipController,
  controllerAs: 'vm',
  bindings: {
    user:                '<',
    onAddRequest:    '&',
    onRemoveRequest: '&'
  }
};

/**
 * The callback function called by {@link common.components.dateTimePicker dateTimePicker} when the
 * user enters a new value.
 *
 * @callback admin.users.components.userMembership.onAddRequest
 *
 * @param {domain.access.Membership} membership - the membership to add
 */

/**
 * The callback function called by {@link common.components.dateTimePicker dateTimePicker} when the
 * user enters a new value.
 *
 * @callback admin.users.components.userMembership.onRemoveRequest
 *
 * @param {domain.access.Membership} membership - the membership to remove
 */


export default ngModule => ngModule.component('userMembership', userMembershipComponent)
