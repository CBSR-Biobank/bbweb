/**
 * AngularJS Component for {@link domain.users.User User} administration.
 *
 * @namespace admin.users.components.usersPagedList
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { PagedListController } from '../../../../../common/controllers/PagedListController';

/*
 * Controller for this component.
 */
class UsersPagedListController extends PagedListController {

  constructor($log,
              $state,
              User,
              UserState,
              userStateLabelService,
              UserCounts,
              gettextCatalog,
              EmailFilter,
              NameFilter,
              StateFilter,
              resourceErrorService) {
    'ngInject';

    const stateData = Object.values(UserState).map((state) => ({
      id: state,
      label: userStateLabelService.stateToLabelFunc(state)
    }));

    super($log,
          $state,
          gettextCatalog,
          resourceErrorService,
          {
            nameFilter:  new NameFilter(),
            emailFilter: new EmailFilter(),
            stateFilter: new StateFilter(stateData, 'all', true)
          },
          [
            { id: 'name',  labelFunc: () => gettextCatalog.getString('Name') },
            { id: 'email', labelFunc: () => gettextCatalog.getString('Email') },
            { id: 'state', labelFunc: () => gettextCatalog.getString('State')  }
          ],
          5);

    Object.assign(this,
                  {
                    User,
                    UserState,
                    userStateLabelService,
                    UserCounts,
                    EmailFilter,
                    NameFilter,
                    StateFilter,
                    resourceErrorService
                  });

    this.stateLabelFuncs = {};
    Object.values(UserState).forEach((state) => {
      this.stateLabelFuncs[state] = userStateLabelService.stateToLabelFunc(state);
    });
  }

  $onInit() {
    super.$onInit();
    this.counts = this.userCounts;
    this.emailFilter = '';
  }

  getItems(options) {
    return this.UserCounts.get()
      .then((counts) => {
        this.userCounts = counts;
        return this.User.list(options);
      });
  }

  getItemIcon(user) {
    if (user.isRegistered()) {
      return 'glyphicon-cog';
    }
    if (user.isActive()) {
      return 'glyphicon-user';
    }
    if (user.isLocked()) {
      return 'glyphicon-lock';
    }
    throw new Error('invalid user state: ' + user.state);
  }
}

/**
 * An AngularJS component that displays {@link domain.access.User Users} in a panel list.
 *
 * The list of users can be filtered and sorted by different fields. The users are displayed in a
 * paged fashion, allowing the logged in user to page through all the users in the system.
 *
 * @memberOf admin.users.components.usersPagedList
 *
 * @param {domain.users.UserCounts} userCount - the counts of users indexed by state.
 */
const usersPagedListComponent = {
  template: require('./usersPagedList.html'),
  controller: UsersPagedListController,
  controllerAs: 'vm',
  bindings: {
    userCounts: '<'
  }
};

export default ngModule => ngModule.component('usersPagedList', usersPagedListComponent)
