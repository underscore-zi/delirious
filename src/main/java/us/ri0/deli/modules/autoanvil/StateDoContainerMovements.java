package us.ri0.deli.modules.autoanvil;

import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public class StateDoContainerMovements implements IState {
    private final IState next;
    private final Map<BlockPos, List<Integer>> movements;

    public StateDoContainerMovements(Map<BlockPos, List<Integer>> movements, IState next) {
        this.next = next;
        this.movements = movements;
    }

    @Override
    public IState next(Context ctx) {
        if (movements.isEmpty()) {
            return next;
        }

        var entry = movements.entrySet().iterator().next();
        var pos = entry.getKey();
        var slots = entry.getValue();
        movements.remove(pos);

        return new StateOpenContainer(pos,
                new StateMoveFromSlot(pos, slots, true,
                new StateCloseScreen(this))
        );
    }
}
